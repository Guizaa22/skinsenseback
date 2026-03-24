import React, { useEffect, useState } from 'react';
import { Calendar, Clock, User, ClipboardList, Loader2 } from 'lucide-react';
import { api } from '@/src/services/api.ts';
import { Appointment, ProfessionalNote } from '../../types.ts';

export default function History() {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [notes, setNotes] = useState<Record<string, ProfessionalNote[]>>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
        try {
            const aptRes = await api.appointments.list().catch(() => ({ data: [] }));
            const aptData = aptRes?.data ?? aptRes ?? [];
            const list = Array.isArray(aptData) ? aptData : [];
            // Show completed AND canceled in history
            const historical = list.filter((a: any) =>
                a.status === 'COMPLETED' ||
                a.status === 'CANCELED' ||
                a.status === 'CANCELLED' ||
                a.status === 'CONFIRMED'
            );
            historical.sort((a, b) => new Date(b.startAt).getTime() - new Date(a.startAt).getTime());
            setAppointments(historical);

            // Fetch notes (silently fail on 403 — notes are EMPLOYEE/ADMIN only)
            const notesMap: Record<string, ProfessionalNote[]> = {};
            await Promise.all(historical.map(async (apt) => {
                const notes = await api.notes.getByAppointment(apt.id).catch(() => ({ data: [] }));
                if (notes.data && notes.data.length > 0) {
                    notesMap[apt.id] = notes.data;
                }
            }));
            setNotes(notesMap);

        } catch (e) {
            console.error("Failed to fetch history", e);
        } finally {
            setLoading(false);
        }
    };
    fetchData();
  }, []);

  if (loading) {
      return <div className="flex justify-center py-20"><Loader2 className="w-8 h-8 animate-spin text-primary" /></div>;
  }

  if (appointments.length === 0) {
      return (
          <div className="flex flex-col items-center justify-center text-center py-16 px-4">
              <ClipboardList className="w-16 h-16 text-gray-200 mb-4" />
              <h2 className="text-xl font-bold text-text">Aucun historique</h2>
              <p className="text-muted mt-2">Vous n'avez pas encore de rendez-vous passés.</p>
          </div>
      );
  }

  return (
    <div className="space-y-6 px-0 md:px-0 animate-in fade-in duration-500">
      <h1 className="text-2xl sm:text-3xl font-black text-text">Historique des soins</h1>
      
      <div className="space-y-6">
        {appointments.map(apt => {
            const start = new Date(apt.startAt);
            const notesForApt = notes[apt.id];
            const displayName = apt.serviceName ?? apt.serviceId?.slice(0, 8) ?? 'Rendez-vous';

            return (
                <div key={apt.id} className="bg-white rounded-2xl p-4 md:p-5 shadow-sm border border-gray-100 overflow-hidden">
                    <div className="flex flex-wrap items-start justify-between gap-2 mb-3">
                        <div>
                            <div className="flex items-center gap-2 mb-2">
                                <span
                                  className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold ${
                                    apt.status === 'CANCELED' || apt.status === 'CANCELLED'
                                      ? 'bg-red-100 text-red-700'
                                      : apt.status === 'COMPLETED'
                                      ? 'bg-gray-100 text-gray-600'
                                      : 'bg-blue-100 text-blue-700'
                                  }`}
                                >
                                  {apt.status === 'CANCELED' || apt.status === 'CANCELLED'
                                    ? 'Cancelled'
                                    : apt.status === 'COMPLETED'
                                    ? 'Terminé'
                                    : 'Confirmé'}
                                </span>
                                <span className="text-xs text-muted">{new Date(apt.createdAt).toLocaleDateString()}</span>
                            </div>
                            <h3 className="text-base sm:text-lg font-bold text-text truncate max-w-[200px] sm:max-w-none">{displayName}</h3>
                        </div>
                        <div className="text-right">
                            <span className="text-lg font-bold text-primary">{apt.price} DT</span>
                        </div>
                    </div>

                    <div className="flex flex-col sm:flex-row sm:flex-wrap gap-1 sm:gap-4 text-sm text-muted py-4 border-t border-b border-gray-50">
                        <div className="flex items-center gap-2">
                            <Calendar className="w-4 h-4" />
                            <span className="capitalize">{start.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}</span>
                        </div>
                        <div className="flex items-center gap-2">
                            <Clock className="w-4 h-4" />
                            <span>{start.toLocaleTimeString('fr-FR', {hour: '2-digit', minute:'2-digit'})}</span>
                        </div>
                        <div className="flex items-center gap-2">
                            <User className="w-4 h-4" />
                            <span>Avec {apt.employeeName}</span>
                        </div>
                    </div>

                    {notesForApt && notesForApt.length > 0 && (
                        <div className="mt-4 bg-primary-light/30 rounded-xl p-4">
                            <h4 className="text-sm font-bold text-primary mb-2 flex items-center gap-2">
                                <ClipboardList className="w-4 h-4" />
                                Notes professionnelles
                            </h4>
                            {notesForApt.map(note => (
                                <div key={note.id} className="text-sm text-text">
                                    <p className="mb-1">{note.noteText}</p>
                                    <p className="text-xs text-muted">Par {note.authorName}</p>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            );
        })}
      </div>
    </div>
  );
}