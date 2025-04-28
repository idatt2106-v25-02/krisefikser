#!/usr/bin/env python3
import requests
import pandas as pd
import matplotlib.pyplot as plt
import os
from datetime import datetime, timedelta
import json
import re

# Get environment variables
GITHUB_TOKEN = os.environ.get("GITHUB_TOKEN")
REPO_FULL_NAME = f"{os.environ.get('REPO_OWNER')}/{os.environ.get('REPO_NAME')}"
OWNER, REPO = REPO_FULL_NAME.split('/')

print(f"Generating velocity chart for {REPO_FULL_NAME}")

headers = {
    "Authorization": f"token {GITHUB_TOKEN}",
    "Accept": "application/vnd.github.v3+json"
}

def get_milestones():
    """Get all milestones (sprints) from the repository"""
    all_milestones = []
    page = 1
    
    while True:
        url = f"https://api.github.com/repos/{OWNER}/{REPO}/milestones?state=all&per_page=100&page={page}"
        response = requests.get(url, headers=headers)
        
        if response.status_code != 200:
            print(f"Error fetching milestones: {response.status_code}")
            print(response.text)
            return all_milestones
        
        milestones = response.json()
        if not milestones:  # No more milestones to fetch
            break
            
        all_milestones.extend(milestones)
        page += 1
    
    return all_milestones

def get_issues_for_milestone(milestone_number):
    """Get all issues for a specific milestone"""
    all_issues = []
    page = 1
    
    while True:
        url = f"https://api.github.com/repos/{OWNER}/{REPO}/issues?milestone={milestone_number}&state=all&per_page=100&page={page}"
        response = requests.get(url, headers=headers)
        
        if response.status_code != 200:
            print(f"Error fetching issues: {response.status_code}")
            print(response.text)
            return all_issues
        
        issues = response.json()
        if not issues:  # No more issues to fetch
            break
            
        all_issues.extend(issues)
        page += 1
    
    return all_issues

def extract_story_points(issue):
    """Extract story points from GitHub project field named 'Story points'"""
    # Get project items for this issue using the GraphQL API
    if 'node_id' in issue:
        try:
            graphql_query = {
                "query": """
                query($issueId: ID!) {
                  node(id: $issueId) {
                    ... on Issue {
                      projectItems(first: 10) {
                        nodes {
                          fieldValues(first: 20) {
                            nodes {
                              ... on ProjectV2ItemFieldNumberValue {
                                field {
                                  ... on ProjectV2FieldCommon {
                                    name
                                  }
                                }
                                number
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """,
                "variables": {
                    "issueId": issue['node_id']
                }
            }

            response = requests.post(
                "https://api.github.com/graphql",
                headers=headers,
                json=graphql_query
            )

            if response.status_code == 200:
                data = response.json()
                
                # Navigate through the response to find the Story points field
                if 'data' in data and 'node' in data['data'] and data['data']['node']:
                    project_items = data['data']['node'].get('projectItems', {}).get('nodes', [])
                    
                    for project_item in project_items:
                        field_values = project_item.get('fieldValues', {}).get('nodes', [])
                        
                        for field_value in field_values:
                            if field_value.get('field', {}).get('name', '').lower() == 'story points':
                                return field_value.get('number', 0)
        except Exception as e:
            print(f"Error getting project data for issue {issue.get('number')}: {e}")
    
    # Fallback method: Look for "Story points" in labels
    for label in issue.get('labels', []):
        label_name = label['name']
        if label_name.startswith('Story points:') or label_name.startswith('Story Points:'):
            try:
                points = label_name.split(':')[1].strip()
                return int(points)
            except:
                pass
    
    # Fallback method: Look for story points in the body
    if issue.get('body'):
        body = issue['body'] or ""
        # Try to find "Story points: X" in the body
        match = re.search(r'story points:\s*(\d+)', body.lower())
        if match:
            return int(match.group(1))
    
    return 0  # Default to 0 if no story points found

def generate_velocity_data():
    """Generate velocity data from GitHub issues and milestones"""
    milestones = get_milestones()
    velocity_data = []
    
    for milestone in milestones:
        sprint_name = milestone['title']
        milestone_number = milestone['number']
        
        # Get completed date or due date
        if milestone['closed_at']:
            end_date = milestone['closed_at']
        elif milestone['due_on']:
            end_date = milestone['due_on']
        else:
            end_date = datetime.now().isoformat()
            
        issues = get_issues_for_milestone(milestone_number)
        
        total_points = 0
        completed_points = 0
        issue_count = 0
        completed_count = 0
        
        for issue in issues:
            # Skip pull requests
            if 'pull_request' in issue:
                continue
                
            issue_count += 1
            points = extract_story_points(issue)
            total_points += points
            
            if issue['state'] == 'closed':
                completed_points += points
                completed_count += 1
        
        # Format the end date
        try:
            end_date_obj = datetime.fromisoformat(end_date.replace('Z', '+00:00'))
            formatted_end_date = end_date_obj.strftime('%Y-%m-%d')
        except:
            formatted_end_date = end_date
        
        velocity_data.append({
            'Sprint': sprint_name,
            'Completed': completed_points,
            'Total': total_points,
            'Completion %': f"{int(completed_points / total_points * 100)}%" if total_points > 0 else "0%",
            'Issues': issue_count,
            'End Date': formatted_end_date
        })
    
    # Sort by end date
    velocity_data.sort(key=lambda x: x['End Date'])
    
    return velocity_data

def create_velocity_chart(data):
    """Create and save velocity chart optimized for wiki display"""
    df = pd.DataFrame(data)
    
    # Create a chart with high resolution and clean styling for wiki
    plt.figure(figsize=(14, 8), dpi=120)
    plt.style.use('ggplot')
    
    sprints = df['Sprint']
    completed = df['Completed']
    total = df['Total']
    
    x = range(len(sprints))
    
    # Create stacked bar chart with wiki-friendly colors
    plt.bar(x, completed, color='#36B37E', label='Completed Points')
    plt.bar(x, total - completed, bottom=completed, color='#DFE1E6', label='Incomplete Points')
    
    # Add velocity trend line
    if len(completed) > 1:
        trend_data = completed.rolling(window=min(3, len(completed)), min_periods=1).mean()
        plt.plot(x, trend_data, color='#FF5630', linestyle='--', linewidth=2, 
                 label='Velocity Trend (3-Sprint Avg)')
    
    # Add data labels on bars
    for i, (c, t) in enumerate(zip(completed, total)):
        if c > 0:  # Only show label if there are completed points
            plt.text(i, c/2, str(int(c)), ha='center', va='center', 
                     color='white', fontweight='bold', fontsize=12)
        if t > c:  # Only show label if there are incomplete points
            plt.text(i, c + (t-c)/2, str(int(t-c)), ha='center', va='center', 
                     color='#505F79', fontweight='bold', fontsize=12)
    
    # Improve x-axis labels for better readability
    plt.xticks(x, sprints, rotation=45, ha='right', fontsize=10)
    plt.xlabel('Sprint', fontsize=12)
    plt.ylabel('Story Points', fontsize=12)
    plt.title('Sprint Velocity Chart', fontsize=16, pad=20)
    
    # Add grid lines for better readability
    plt.grid(axis='y', linestyle='--', alpha=0.5)
    
    # Add legend with better positioning
    plt.legend(loc='upper left', frameon=True)
    
    # Ensure proper layout
    plt.tight_layout()
    
    # Add timestamp to chart
    plt.figtext(0.01, 0.01, f"Generated: {datetime.now().strftime('%Y-%m-%d')}", 
                fontsize=8, color='#505F79')
    
    # Save the chart with high quality for wiki display
    chart_path = 'velocity_chart.png'
    plt.savefig(chart_path, dpi=120, bbox_inches='tight')
    print(f"Velocity chart saved as '{chart_path}'")
    
    # Save the data to CSV with columns optimized for wiki table display
    df.to_csv('velocity_data.csv', index=False)
    print("Velocity data saved as 'velocity_data.csv'")
    
    return df

if __name__ == "__main__":
    print("Fetching GitHub data...")
    velocity_data = generate_velocity_data()
    
    if velocity_data:
        print(f"Data collected for {len(velocity_data)} sprints")
        df = create_velocity_chart(velocity_data)
        print("\nVelocity Summary:")
        print(df[['Sprint', 'Completed', 'Total', 'Completion %']].to_string(index=False))
    else:
        print("No data collected. Check your GitHub token and repository settings.")
