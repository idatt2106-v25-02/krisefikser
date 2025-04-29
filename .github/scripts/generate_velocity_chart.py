import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import sys
import requests
import os

def create_velocity_chart():
    # GitHub Config
    # Use environment variable for token instead of hardcoding
    TOKEN = os.environ.get("GITHUB_TOKEN", "")
    if not TOKEN:
        print("Error: GitHub token not provided. Set the GITHUB_TOKEN environment variable.")
        sys.exit(1)
    
    REPO_OWNER = "idatt2106-v25-02"
    REPO_NAME = "krisefikser"
    PROJECT_NUMBER = 4  # Find this in your project's URL

    # Updated GraphQL query
    query = """
    query GetProjectItems($owner: String!, $repo: String!, $projectNumber: Int!) {
    repository(owner: $owner, name: $repo) {
        projectV2(number: $projectNumber) {
        items(first: 100) {
            nodes {
            content {
                ... on Issue {
                title
                number
                state
                milestone {
                    title
                }
                }
            }
            fieldValues(first: 10) {
                nodes {
                ... on ProjectV2ItemFieldSingleSelectValue {
                    field {
                    ... on ProjectV2FieldCommon {
                        name
                    }
                    }
                    name
                }
                ... on ProjectV2ItemFieldNumberValue {
                    field {
                    ... on ProjectV2FieldCommon {
                        name
                    }
                    }
                    number
                }
                ... on ProjectV2ItemFieldTextValue {
                    field {
                    ... on ProjectV2FieldCommon {
                        name
                    }
                    }
                    text
                }
                }
            }
            }
        }
        }
    }
    }
    """

    # Make sure TOKEN doesn't have any quotes
    TOKEN = TOKEN.strip('"\'')
    headers = {"Authorization": f"Bearer {TOKEN}"}
    variables = {
        "owner": REPO_OWNER,
        "repo": REPO_NAME,
        "projectNumber": PROJECT_NUMBER
    }

    print("Fetching data from GitHub API...")
    response = requests.post(
        "https://api.github.com/graphql",
        json={"query": query, "variables": variables},
        headers=headers
    )

    if response.status_code != 200:
        print(f"Error accessing GitHub API: {response.status_code}")
        print(response.text)
        sys.exit(1)

    data = response.json()

    # Check for errors in the response
    if "errors" in data:
        print(f"Error in GraphQL query: {data['errors']}")
        sys.exit(1)

    # Parse response
    items = data["data"]["repository"]["projectV2"]["items"]["nodes"]
    rows = []
    for item in items:
        if not item["content"]:
            continue
            
        issue = item["content"]
        title = issue["title"]
        state = issue["state"]
        milestone = issue["milestone"]["title"] if issue.get("milestone") else "None"
        story_points = "None"
        
        # Handle all field types
        for field in item["fieldValues"]["nodes"]:
            if "field" not in field:
                continue
                
            field_name = field["field"]["name"]
            if field_name == "Story points":
                if "number" in field:
                    story_points = field["number"]
                elif "name" in field:  # In case it's stored as single select
                    story_points = field["name"]
        
        rows.append([title, milestone, story_points, state])

    # Save as CSV
    csv_file_path = "issues_with_story_points.csv"
    df = pd.DataFrame(rows, columns=["Title", "Milestone", "Story Points", "Status"])
    df.to_csv(csv_file_path, index=False)
    
    print(f"Successfully saved data to {csv_file_path}")
    print(f"Found {len(df)} tasks in the data")
    
    # Convert 'Story Points' to numeric, replacing None/NaN with 0
    df['Story Points'] = pd.to_numeric(df['Story Points'], errors='coerce').fillna(0)
    
    # Extract sprint information
    df['Sprint'] = df['Milestone'].str.extract(r'(Sprint \d+)', expand=False)
    
    # Filter out rows without a specific sprint
    sprint_df = df[df['Sprint'].notna()].copy()
    
    # Identify sprint backlog items (those starting with '[Sprint Backlog]')
    sprint_df['IsSprintBacklog'] = sprint_df['Title'].str.startswith('[Sprint Backlog]')
    
    # Group by Sprint to get planned points (only from Sprint Backlog items)
    planned_points = sprint_df[sprint_df['IsSprintBacklog']].groupby('Sprint')['Story Points'].sum()
    
    # Completed points are those with CLOSED status (regardless of whether they're Sprint Backlog items)
    completed_points = sprint_df[sprint_df['Status'] == 'CLOSED'].groupby('Sprint')['Story Points'].sum()
    
    # Ensure all sprints in planned are also in completed (with 0 if needed)
    all_sprints = set(planned_points.index) | set(completed_points.index)
    for sprint in all_sprints:
        if sprint not in planned_points.index:
            planned_points[sprint] = 0
        if sprint not in completed_points.index:
            completed_points[sprint] = 0
    
    # Sort sprints naturally
    sprints = sorted(all_sprints, key=lambda x: int(x.split()[1]) if x.split()[1].isdigit() else 0)
    
    if not sprints:
        print("No sprint data found. Exiting.")
        return
    
    print(f"Creating velocity chart for sprints: {', '.join(sprints)}")
    
    # Create the bar chart - use Agg backend to avoid display issues in headless environments
    plt.switch_backend('Agg')
    plt.figure(figsize=(12, 6))
    
    # Set width of bars
    bar_width = 0.35
    
    # Set position of bars on x-axis
    r1 = np.arange(len(sprints))
    r2 = [x + bar_width for x in r1]
    
    # Create bars
    plt.bar(r1, [planned_points[sprint] for sprint in sprints], width=bar_width, label='Planned (Sprint Backlog)', color='skyblue')
    plt.bar(r2, [completed_points[sprint] for sprint in sprints], width=bar_width, label='Completed', color='green')
    
    # Add labels and title
    plt.xlabel('Sprints', fontweight='bold')
    plt.ylabel('Story Points', fontweight='bold')
    plt.title('Velocity Chart: Planned vs Completed Story Points by Sprint')
    plt.xticks([r + bar_width/2 for r in range(len(sprints))], sprints)
    
    # Add completion percentage labels
    for i, sprint in enumerate(sprints):
        planned = planned_points[sprint]
        completed = completed_points[sprint]
        
        if planned > 0:
            completion_pct = (completed / planned) * 100
            plt.text(i + bar_width/2, max(planned, completed) + 0.5, 
                     f"{completion_pct:.1f}%", 
                     ha='center', va='bottom')
    
    # Add a grid for better readability
    plt.grid(axis='y', linestyle='--', alpha=0.7)
    
    # Add a legend
    plt.legend()
    
    # Display statistics
    total_planned = planned_points.sum()
    total_completed = completed_points.sum()
    completion_rate = (total_completed / total_planned) * 100 if total_planned > 0 else 0
    
    # Calculate sprint backlog items count
    sprint_backlog_count = sprint_df['IsSprintBacklog'].sum()
    completed_backlog_count = sprint_df[(sprint_df['IsSprintBacklog']) & (sprint_df['Status'] == 'CLOSED')].shape[0]
    
    stats_text = (f"Total Planned Points (Sprint Backlog): {total_planned}\n"
                 f"Total Completed Points: {total_completed}\n"
                 f"Overall Completion Rate: {completion_rate:.1f}%\n"
                 f"Sprint Backlog Items: {sprint_backlog_count}, Completed: {completed_backlog_count}")
    
    plt.figtext(0.02, 0.02, stats_text, wrap=True, fontsize=9)
    
    # Save the chart
    output_file = "velocity_chart.png"
    plt.savefig(output_file, dpi=300, bbox_inches='tight')
    print(f"Velocity chart saved as {output_file}")

if __name__ == "__main__":
    create_velocity_chart()
