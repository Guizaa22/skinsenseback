export interface User {
  id: string;
  fullName: string;
  email: string;
  phone: string;
  role: 'ADMIN' | 'EMPLOYEE' | 'CLIENT';
  isActive: boolean;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  data: {
    accessToken: string;
    refreshToken: string;
    expiresIn: number;
    tokenType: string;
  };
}

export interface Service {
  id: string;
  name: string;
  description: string;
  durationMinutes: number;
  price: number;
  isActive: boolean;
  image?: string; // Frontend specific, not in backend DTO but needed for UI
}

export interface Employee {
  id: string;
  fullName: string;
  email: string;
  role: 'EMPLOYEE';
  specialties: string[]; // List of service IDs or Specialty IDs
  image?: string; // Frontend specific
  jobTitle?: string; // Frontend specific for display
}

export type AppointmentStatus = 'CONFIRMED' | 'CANCELED' | 'COMPLETED' | 'PENDING';

export interface Appointment {
  id: string;
  clientId: string;
  employeeId: string;
  serviceId: string;
  startAt: string; // ISO 8601 with timezone
  endAt: string; // ISO 8601 with timezone
  status: AppointmentStatus;
  cancellationReason?: string;
  createdAt: string;
  updatedAt: string;
  // Hydrated fields for UI convenience (in a real app, these might come from separate calls or expanded DTOs)
  serviceName?: string;
  employeeName?: string;
  price?: number;
}

export interface ProfessionalNote {
  id: string;
  appointmentId: string;
  authorId: string;
  authorName: string;
  noteText: string;
  createdAt: string;
}

export interface WorkingTimeSlot {
  id: string;
  employeeId: string;
  dayOfWeek: string;
  startTime: string; // HH:mm:ss
  endTime: string; // HH:mm:ss
}

export interface TimeSlot {
  startAt: string; // ISO
  endAt: string; // ISO
}

export interface ClientFile {
  id: string;
  clientId: string;
  intake: {
    howDidYouHearAboutUs: string;
    consultationReason: string;
    objective: string;
    careType: string;
    skincareRoutine: string;
    habits: string;
    dateOfBirth?: string;
    address?: string;
    profession?: string;
    howFoundUs?: string | string[];
    consultationTypes?: string | string[];
    consultationTypeAutre?: string;
  };
  medicalHistory: {
    medicalBackground: string;
    currentTreatments: string;
    allergiesAndReactions: string;
    grossesse?: boolean;
    diabete?: boolean;
    maladieAutoImmune?: boolean;
    troublesHormonaux?: boolean;
    problemsCicatrisation?: boolean;
    herpes?: boolean;
    epilepsie?: boolean;
    cancer?: boolean;
    maladiesDermatologiques?: boolean;
    interventionChirurgicale?: boolean;
    autreAntecedent?: string;
    isotretinoine?: boolean;
    corticoides?: boolean;
    anticoagulants?: boolean;
    hormones?: boolean;
    antibiotiques?: boolean;
    dateArretMedicament?: string;
    allergiesMedicamenteuses?: boolean;
    allergiesCosmetiques?: boolean;
    reactionsPostSoinsAnterieures?: boolean;
    detailsAllergies?: string;
    tabac?: boolean;
    expositionSolaire?: boolean;
    cabineUV?: boolean;
    sportIntensif?: boolean;
    stressImportant?: boolean;
  };
  aestheticProcedureHistory: {
    procedures: string;
    peeling?: boolean;
    laser?: boolean;
    microneedlingHistory?: boolean;
    injections?: boolean;
    dateDernierSoin?: string;
    detailsInjections?: string;
  };
  photoConsentForFollowUp: boolean;
  photoConsentForFollowup?: boolean;
  photoConsentForMarketing: boolean;
  consentGiven?: boolean;
  consentDate?: string;
  lastUpdated?: string;
}

export interface ClientConsent {
  id: string;
  clientId: string;
  smsOptIn: boolean;
  smsUnsubToken: string;
}