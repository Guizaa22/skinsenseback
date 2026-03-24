import React, { useEffect, useState } from 'react';
import { Calendar, Clock, User, Trash2, Sparkles, ChevronRight, Edit2, Loader2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/src/context/AuthContext.tsx';
import { api } from '@/src/services/api.ts';
import { Appointment, ClientFile } from '../../types.ts';

export default function Dashboard() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [clientFile, setClientFile] = useState<ClientFile | null>(null);
  const [loading, setLoading] = useState(true);
  const [reschedulingApt, setReschedulingApt] = useState<any | null>(null);
  const [newDate, setNewDate] = useState('');
  const [availableSlots, setAvailableSlots] = useState<any[]>([]);
  const [selectedSlot, setSelectedSlot] = useState<any | null>(null);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [rescheduling, setRescheduling] = useState(false);

  const fetchData = async () => {
    try {
      const aptRes = await api.appointments.list().catch(() => ({ data: [] }));
      const aptData = (aptRes as any)?.data ?? aptRes ?? [];
      setAppointments(Array.isArray(aptData) ? aptData : []);
    } catch { setAppointments([]); }

    try {
      const fileRes = await api.clientFile.get().catch(() => ({ data: null }));
      if ((fileRes as any)?.data) {
        const rawFile = (fileRes as any).data;
        setClientFile({ ...rawFile, photoConsentForFollowUp: rawFile.photoConsentForFollowup ?? rawFile.photoConsentForFollowUp });
      }
    } catch { setClientFile(null); }

    setLoading(false);
  };

  useEffect(() => {
    fetchData();
  }, []);

  useEffect(() => {
    const interval = setInterval(fetchData, 15000);
    return () => clearInterval(interval);
  }, []);

  const handleReschedule = async (apt: any) => {
    setReschedulingApt(apt);
    setSelectedSlot(null);
    const today = new Date().toISOString().split('T')[0];
    setNewDate(today);
    setLoadingSlots(true);
    try {
      const res = await fetch(`/api/availability?serviceId=${apt.serviceId}&date=${today}`, {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
      });
      const data = await res.json();
      setAvailableSlots(data?.data?.availableSlots ?? []);
    } catch { setAvailableSlots([]); }
    finally { setLoadingSlots(false); }
  };

  const handleDateChange = async (date: string) => {
    if (!reschedulingApt) return;
    setNewDate(date);
    setSelectedSlot(null);
    setLoadingSlots(true);
    try {
      const res = await fetch(`/api/availability?serviceId=${reschedulingApt.serviceId}&date=${date}`, {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
      });
      const data = await res.json();
      setAvailableSlots(data?.data?.availableSlots ?? []);
    } catch { setAvailableSlots([]); }
    finally { setLoadingSlots(false); }
  };

  const handleConfirmReschedule = async () => {
    if (!reschedulingApt || !selectedSlot) return;
    setRescheduling(true);
    try {
      await fetch(`/api/appointments/${reschedulingApt.id}/cancel`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({ cancellationReason: 'Reprogrammé par le client' })
      });
      await fetch('/api/appointments', {
        method: 'POST',
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({ serviceId: reschedulingApt.serviceId, startAt: selectedSlot.startAt })
      });
      setReschedulingApt(null);
      fetchData();
    } catch (err) { console.error('Reschedule failed', err); }
    finally { setRescheduling(false); }
  };

  if (loading) {
      return <div className="flex h-full items-center justify-center"><Loader2 className="w-8 h-8 animate-spin" style={{ color: '#A17969' }} /></div>;
  }

  const nextAppointments = (appointments ?? []).filter((a: any) => a.status === 'CONFIRMED' || a.status === 'PENDING');
  const fileIncomplete = !clientFile?.photoConsentForFollowUp && !clientFile?.photoConsentForMarketing;

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between sm:items-end gap-3 mb-6">
        <div>
          <h1
            className="mb-1"
            style={{
              fontFamily: "'Cormorant Garamond', serif",
              fontSize: 'clamp(1.8rem, 6vw, 2.5rem)',
              fontWeight: 300,
              color: '#534741',
            }}
          >
            Bonjour, {user?.fullName.split(' ')[0]}
          </h1>
          <p className="text-muted">Prête pour votre prochaine séance de bien-être ?</p>
        </div>
      </div>

      {/* Complete your file banner */}
      {fileIncomplete && (
        <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 flex items-center justify-between gap-4">
          <p className="text-sm font-semibold text-amber-800">Complétez votre dossier client (consentements photos).</p>
          <button onClick={() => navigate('/app/client-file')} className="shrink-0 px-4 py-2 bg-amber-600 text-white text-sm font-bold rounded-lg hover:bg-amber-700 transition-colors">
            Compléter
          </button>
        </div>
      )}

      {/* Main Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 md:gap-8">
        
        {/* Appointments Column */}
        <div className="lg:col-span-2 space-y-6">
          <div className="flex justify-between items-center">
            <h2 className="text-xl font-bold" style={{ color: '#534741' }}>Mes prochains rendez-vous</h2>
            <div className="flex bg-white rounded-lg border p-1" style={{ borderColor: '#E9DCD6' }}>
                <button className="px-3 py-1 text-xs font-bold rounded shadow-sm" style={{ backgroundColor: '#F5EDE8', color: '#534741' }}>Vue Liste</button>
                <button className="px-3 py-1 text-xs font-medium hover:opacity-80" style={{ color: '#998675' }}>Calendrier</button>
            </div>
          </div>

          <div className="space-y-4">
            {nextAppointments.length === 0 && (
                <div className="rounded-2xl p-8 text-center border" style={{ backgroundColor: '#FFFFFF', borderColor: '#E9DCD6', borderRadius: '1rem' }}>
                    <p className="text-muted mb-4">Aucun rendez-vous à venir (total: {appointments.length}).</p>
                    <button onClick={() => navigate('/app/booking')} className="font-bold text-sm" style={{ color: '#A17969' }}>Prendre un rendez-vous</button>
                </div>
            )}
            {nextAppointments.map((apt) => {
              const start = new Date(apt.startAt);
              const durationMinutes = (new Date(apt.endAt).getTime() - start.getTime()) / 60000;

              return (
                <div key={apt.id} className="p-5 flex flex-col sm:flex-row gap-6 transition-all hover:shadow-md rounded-2xl" style={{ backgroundColor: '#FFFFFF', border: '1px solid #E9DCD6', borderRadius: '1rem' }}>
                  <div className="flex-1 flex flex-col justify-between">
                    <div>
                      <div className="mb-2">
                        <span
                          className="text-xs font-bold px-2 py-1 rounded-full border"
                          style={apt.status === 'PENDING'
                            ? { backgroundColor: '#FDF3EE', color: '#A17969', borderColor: '#CEAA9A' }
                            : { backgroundColor: '#F0F7F0', color: '#3B6D11', borderColor: 'transparent' }
                          }
                        >
                          {apt.status === 'PENDING' ? '⏳ En attente' : '✓ Confirmé'}
                        </span>
                      </div>
                      
                      <h3 className="text-lg font-bold mb-3" style={{ color: '#534741' }}>{(apt as any).serviceName ?? 'Rendez-vous'}</h3>
                      
                      <div className="space-y-2">
                        <div className="flex items-center gap-2 text-sm text-muted flex-wrap">
                          <Calendar className="w-4 h-4" />
                          <span className="capitalize">{start.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}</span>
                        </div>
                        <div className="flex items-center gap-2 text-sm text-muted flex-wrap">
                          <Clock className="w-4 h-4" />
                          <span>{start.toLocaleTimeString('fr-FR', {hour: '2-digit', minute:'2-digit'})} ({Math.floor(durationMinutes / 60)}h {durationMinutes % 60 > 0 ? `${durationMinutes % 60}min` : '00min'})</span>
                        </div>
                        <div className="flex items-center gap-2 text-sm text-muted flex-wrap">
                          <User className="w-4 h-4" />
                          <span>Avec {apt.employeeName}</span>
                        </div>
                      </div>
                    </div>

                    <div className="flex flex-col xs:flex-row gap-2 mt-4 pt-4 border-t border-gray-50">
                      <button
                        onClick={() => handleReschedule(apt)}
                        className="flex-1 h-11 xs:h-9 flex items-center justify-center rounded-lg bg-primary/10 text-primary text-sm font-bold hover:bg-primary/20 transition-colors"
                      >
                        Modifier
                      </button>
                      <button className="h-9 w-9 flex items-center justify-center rounded-lg bg-gray-50 text-gray-400 hover:text-red-500 hover:bg-red-50 transition-colors">
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Sidebar Widgets Column */}
        <div className="space-y-6">
            
            {/* Skin Profile Summary */}
            {clientFile && (
                <div
                  className="rounded-2xl p-6 shadow-sm border relative overflow-hidden"
                  style={{ backgroundColor: '#FFFFFF', borderColor: '#E9DCD6', borderRadius: '1rem' }}
                >
                    <img
                      src="/skinsense-logo-emblem.png"
                      alt=""
                      style={{
                        position: 'absolute',
                        right: '-12px',
                        bottom: '-12px',
                        width: '90px',
                        opacity: 0.06,
                        pointerEvents: 'none',
                      }}
                    />
                    <div className="flex justify-between items-start mb-4">
                        <div>
                            <h3 className="text-lg font-bold" style={{ fontFamily: "'Cormorant Garamond', serif", color: '#534741' }}>Aperçu de mon dossier</h3>
                            <p className="text-xs text-muted mt-1">
                                Dernière mise à jour : {new Date(clientFile.lastUpdated).toLocaleDateString()}
                            </p>
                        </div>
                        <button onClick={() => navigate('/app/client-file')} className="p-2 rounded-lg transition-colors hover:opacity-90" style={{ backgroundColor: '#EBCDC4', color: '#A17969' }}>
                            <Edit2 className="w-4 h-4" />
                        </button>
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                        <div className="p-3 bg-background rounded-xl">
                            <p className="text-[10px] font-bold text-muted uppercase mb-1">Type de Peau</p>
                            <p className="text-sm font-semibold text-text">{clientFile.intake.careType || 'Non renseigné'}</p>
                        </div>
                        <div className="p-3 bg-background rounded-xl">
                            <p className="text-[10px] font-bold text-muted uppercase mb-1">Objectif</p>
                            <p className="text-sm font-semibold text-text truncate">{clientFile.intake.objective || 'Non renseigné'}</p>
                        </div>
                    </div>
                </div>
            )}

            {/* Recommendation */}
            <div className="relative overflow-hidden rounded-2xl p-6 text-center border-2 border-dashed" style={{ background: 'linear-gradient(to bottom right, #F5EDE8, #EBCDC4)', borderColor: '#CEAA9A' }}>
                <div className="relative z-10 flex flex-col items-center">
                    <Sparkles className="w-8 h-8 mb-3" style={{ color: '#A17969' }} />
                    <h3 className="font-bold mb-1" style={{ color: '#534741' }}>Recommandé pour vous</h3>
                    <p className="text-xs text-muted mb-4">Basé sur vos derniers soins</p>
                    <p className="text-sm font-bold mb-4" style={{ color: '#534741' }}>Cure de Vitamine C - 3 séances</p>
                    <button className="w-full py-2 text-white text-xs font-bold rounded-lg shadow-md hover:opacity-90 transition-opacity" style={{ backgroundColor: '#A17969' }}>
                        Découvrir l'offre
                    </button>
                </div>
            </div>

        </div>
      </div>

      {reschedulingApt && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
          <div className="bg-white rounded-t-2xl sm:rounded-2xl p-5 sm:p-6 w-full sm:max-w-md shadow-2xl">
            <h3 style={{ fontFamily: 'Cormorant Garamond, serif', fontSize: '1.5rem', color: '#534741', marginBottom: '1rem' }}>
              Reprogrammer le rendez-vous
            </h3>
            <p className="text-sm mb-4" style={{ color: '#998675' }}>
              {reschedulingApt.serviceName ?? 'Soin'} — choisissez un nouveau créneau
            </p>

            <div className="mb-4">
              <label className="text-xs font-medium mb-1 block" style={{ color: '#736357' }}>Date</label>
              <input
                type="date"
                value={newDate}
                min={new Date().toISOString().split('T')[0]}
                onChange={e => handleDateChange(e.target.value)}
                className="w-full px-3 py-2 rounded-lg border text-sm"
                style={{ borderColor: '#E9DCD6', backgroundColor: '#FDF8F5', color: '#534741' }}
              />
            </div>

            <div className="mb-6">
              <label className="text-xs font-medium mb-2 block" style={{ color: '#736357' }}>
                Créneaux disponibles
              </label>
              {loadingSlots ? (
                <p className="text-sm text-center py-4" style={{ color: '#998675' }}>Chargement...</p>
              ) : availableSlots.length === 0 ? (
                <p className="text-sm text-center py-4" style={{ color: '#998675' }}>Aucun créneau disponible ce jour</p>
              ) : (
                <div className="grid grid-cols-3 gap-2">
                  {availableSlots.map((slot: any, i: number) => (
                      <button
                      key={i}
                      onClick={() => setSelectedSlot(slot)}
                      className="py-2 px-3 rounded-lg text-xs font-medium transition-all"
                      style={{
                        backgroundColor: selectedSlot?.startAt === slot.startAt ? '#A17969' : '#F5EDE8',
                        color: selectedSlot?.startAt === slot.startAt ? 'white' : '#736357',
                          border: `1px solid ${selectedSlot?.startAt === slot.startAt ? '#A17969' : '#E9DCD6'}`,
                          minHeight: '40px',
                      }}
                    >
                      {new Date(slot.startAt).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })}
                    </button>
                  ))}
                </div>
              )}
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => setReschedulingApt(null)}
                className="flex-1 py-3 rounded-xl text-sm font-medium"
                style={{ border: '1px solid #E9DCD6', color: '#998675' }}
              >
                Annuler
              </button>
              <button
                onClick={handleConfirmReschedule}
                disabled={!selectedSlot || rescheduling}
                className="flex-1 py-3 rounded-xl text-sm font-bold transition-all disabled:opacity-50"
                style={{ backgroundColor: '#A17969', color: 'white' }}
              >
                {rescheduling ? 'En cours...' : 'Confirmer'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}