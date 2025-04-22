// Trondheim center coordinates
export const TRONDHEIM_CENTER: [number, number] = [63.4305, 10.3951];

// Shelter model
export interface Shelter {
    id: number;
    name: string;
    position: [number, number];
    capacity: number;
}

// Crisis area model
export interface CrisisArea {
    id: number;
    name: string;
    center: [number, number];
    radius: number;
    severity: number;
    color: string;
    description: string;
}

// Mock emergency shelters around Trondheim
export const shelters: Shelter[] = [
    { id: 1, name: 'Nidarøhallen Shelter', position: [63.4278, 10.3994], capacity: 500 },
    { id: 2, name: 'Leangen Shelter', position: [63.4406, 10.4591], capacity: 300 },
    { id: 3, name: 'Strindheim Shelter', position: [63.4367, 10.4334], capacity: 250 },
    { id: 4, name: 'Lade Shelter', position: [63.4494, 10.4231], capacity: 400 },
    { id: 5, name: 'Byåsen Shelter', position: [63.4103, 10.3401], capacity: 350 },
    { id: 6, name: 'Tiller Shelter', position: [63.3667, 10.3924], capacity: 600 },
    { id: 7, name: 'Heimdal Shelter', position: [63.3505, 10.3454], capacity: 450 },
    { id: 8, name: 'Moholt Shelter', position: [63.4142, 10.4355], capacity: 700 },
];

// Mock crisis areas with severity levels (1-low, 2-medium, 3-high)
export const crisisAreas: CrisisArea[] = [
    {
        id: 1,
        name: 'Flooding Risk Area',
        center: [63.4330, 10.4050],
        radius: 1200,
        severity: 1,
        color: '#FFC107', // Yellow
        description: 'Moderate flooding risk due to rising water levels'
    },
    {
        id: 2,
        name: 'Storm Impact Zone',
        center: [63.4103, 10.3701],
        radius: 800,
        severity: 2,
        color: '#FF9800', // Orange
        description: 'Storm damage reported. Caution advised when traveling'
    },
    {
        id: 3,
        name: 'Gas Leak Hazard',
        center: [63.4194, 10.4431],
        radius: 500,
        severity: 3,
        color: '#F44336', // Red
        description: 'Dangerous gas leak reported. Evacuation in progress'
    }
]; 