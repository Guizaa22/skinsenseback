import { Service, Employee } from './types';

export const services: Service[] = [
  {
    id: 's1',
    name: 'Soin Visage Hydrafacial',
    durationMinutes: 90,
    price: 120,
    isActive: true,
    description: 'Le soin ultime pour une peau parfaitement nettoyée, hydratée et lumineuse.',
    image: 'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?auto=format&fit=crop&q=80&w=300&h=200'
  },
  {
    id: 's2',
    name: 'Massage Signature Relaxant',
    durationMinutes: 60,
    price: 95,
    isActive: true,
    description: 'Un modelage relaxant à l\'huile pour un lâcher-prise total.',
    image: 'https://images.unsplash.com/photo-1544161515-4ab6ce6db874?auto=format&fit=crop&q=80&w=300&h=200'
  },
  {
    id: 's3',
    name: 'Microneedling',
    durationMinutes: 45,
    price: 150,
    isActive: true,
    description: 'Relancez la production de collagène pour une peau plus ferme.',
    image: 'https://images.unsplash.com/photo-1616394584738-fc6e612e71b9?auto=format&fit=crop&q=80&w=300&h=200'
  },
  {
    id: 's4',
    name: 'Gommage Corps',
    durationMinutes: 45,
    price: 55,
    isActive: true,
    description: 'Exfoliation délicate aux sels marins.',
    image: 'https://images.unsplash.com/photo-1555820585-c5ae44394b79?auto=format&fit=crop&q=80&w=300&h=200'
  }
];

export const employees: Employee[] = [
  {
    id: 'e1',
    fullName: 'Marie L.',
    email: 'marie@example.com',
    role: 'EMPLOYEE',
    jobTitle: 'Esthéticienne Senior',
    specialties: ['s1', 's3'],
    image: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=150&h=150'
  },
  {
    id: 'e2',
    fullName: 'Thomas R.',
    email: 'thomas@example.com',
    role: 'EMPLOYEE',
    jobTitle: 'Masseur',
    specialties: ['s2', 's4'],
    image: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&q=80&w=150&h=150'
  },
  {
    id: 'e3',
    fullName: 'Sophie M.',
    email: 'sophie@example.com',
    role: 'EMPLOYEE',
    jobTitle: 'Experte Visage',
    specialties: ['s1', 's3'],
    image: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&q=80&w=150&h=150'
  }
];
