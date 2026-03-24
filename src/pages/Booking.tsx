import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft, ChevronRight, Clock, CheckCircle2, Calendar as CalIcon, AlertCircle, Loader2 } from 'lucide-react';
import { Service, TimeSlot } from '../../types.ts';
import { api, fetchSecureImage } from '@/src/services/api.ts';

const steps = ['Service', 'Créneau', 'Confirmation'];

export default function Booking() {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(0);
  const [loading, setLoading] = useState(false);

  // Data State
  const [services, setServices] = useState<Service[]>([]);
  const [servicePhotos, setServicePhotos] = useState<Record<string, string>>({});
  const [availableSlots, setAvailableSlots] = useState<TimeSlot[]>([]);
  const [dateSlotsMap, setDateSlotsMap] = useState<Record<string, boolean>>({});
  const [bookingError, setBookingError] = useState('');

  // Selection State
  const [selectedService, setSelectedService] = useState<Service | null>(null);
  const [selectedDate, setSelectedDate] = useState<string>(new Date().toISOString().split('T')[0]);
  const [selectedSlot, setSelectedSlot] = useState<TimeSlot | null>(null);
  const [previewService, setPreviewService] = useState<Service | null>(null);
  const [calendarMonth, setCalendarMonth] = useState<Date>(() => {
    const d = new Date();
    d.setDate(1);
    return d;
  });

  useEffect(() => {
    // Initial data fetch
    setLoading(true);
    api.services.list()
        .then(sRes => {
            const svcData = sRes.data ?? sRes ?? [];
            setServices(Array.isArray(svcData) ? svcData : []);
            // Fetch first photo for each service
            const svcList = Array.isArray(svcData) ? svcData : [];
            svcList.forEach((svc: any) => {
              if (!svc.id) return;
              api.photos.getServicePhotos(svc.id)
                .then((res: any) => {
                  const photos = Array.isArray(res?.data) ? res.data : [];
                  if (photos.length > 0) {
                    fetchSecureImage(api.photos.getImageUrl(photos[0].url))
                      .then(blobUrl => {
                        if (blobUrl) setServicePhotos(prev => ({ ...prev, [svc.id]: blobUrl }));
                      });
                  }
                })
                .catch(() => {});
            });
        })
        .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
      // Fetch slots when date or service changes (no employee — backend auto-assigns)
      if (selectedService && currentStep === 1) {
          setLoading(true);
          api.availability.getSlots(null, selectedService.id, selectedDate)
            .then(res => setAvailableSlots(res.data?.availableSlots || []))
            .catch(err => console.error(err))
            .finally(() => setLoading(false));
      }
  }, [selectedDate, selectedService, currentStep]);

  useEffect(() => {
    if (!selectedService || currentStep !== 1) return;
    const year = calendarMonth.getFullYear();
    const month = calendarMonth.getMonth();
    const lastDay = new Date(year, month + 1, 0).getDate();
    const checks: Promise<void>[] = [];
    for (let day = 1; day <= lastDay; day++) {
      const d = new Date(year, month, day);
      const dateStr = d.toISOString().split('T')[0];
      checks.push(
        api.availability.getSlots(null, selectedService.id, dateStr)
          .then(res => {
            const slots = res.data?.availableSlots ?? [];
            setDateSlotsMap(prev => ({ ...prev, [dateStr]: slots.length > 0 }));
          })
          .catch(() => {
            setDateSlotsMap(prev => ({ ...prev, [dateStr]: false }));
          })
      );
    }
    Promise.all(checks);
  }, [selectedService, currentStep, calendarMonth]);

  useEffect(() => {
    if (selectedService && currentStep === 1) {
      const d = new Date();
      d.setDate(1);
      setCalendarMonth(d);
    }
  }, [selectedService?.id]);

  const handleNext = async () => {
    if (currentStep === 2) {
        // Confirm Booking
        setLoading(true);
        try {
            await api.appointments.create({
                serviceId: selectedService!.id,
                startAt: selectedSlot!.startAt,
            });
            navigate('/app');
        } catch (err: any) {
            console.error(err);
            const msg = err?.message || '';
            if (msg.includes('409') || msg.includes('employee') || msg.includes('available')) {
              setBookingError('Ce créneau n\'est plus disponible. Veuillez choisir un autre horaire.');
              setSelectedSlot(null);
              setCurrentStep(1);
            } else {
              setBookingError(msg || 'Erreur lors de la réservation. Veuillez réessayer.');
            }
        } finally {
            setLoading(false);
        }
    } else {
        if (currentStep < steps.length - 1) setCurrentStep(currentStep + 1);
    }
  };

  const handleBack = () => {
    if (currentStep > 0) setCurrentStep(currentStep - 1);
    else navigate('/app');
  };

  const isStepValid = () => {
    if (currentStep === 0) return !!selectedService;
    if (currentStep === 1) return !!selectedSlot;
    return true;
  };

  // Build month grid for calendar (Mon–Sun, 6 rows)
  const getCalendarMonthGrid = () => {
    const year = calendarMonth.getFullYear();
    const month = calendarMonth.getMonth();
    const first = new Date(year, month, 1);
    const last = new Date(year, month + 1, 0);
    const daysInMonth = last.getDate();
    const startOffset = (first.getDay() + 6) % 7;
    const today = new Date().toISOString().split('T')[0];
    const cells: { dateStr: string | null; day: number; isCurrentMonth: boolean }[] = [];
    const total = 42;
    for (let i = 0; i < total; i++) {
      if (i < startOffset) {
        cells.push({ dateStr: null, day: 0, isCurrentMonth: false });
      } else if (i >= startOffset + daysInMonth) {
        cells.push({ dateStr: null, day: 0, isCurrentMonth: false });
      } else {
        const day = i - startOffset + 1;
        const d = new Date(year, month, day);
        const dateStr = d.toISOString().split('T')[0];
        cells.push({ dateStr, day, isCurrentMonth: true });
      }
    }
    return { cells, today };
  };
  const { cells: calendarCells, today: todayStr } = currentStep === 1 ? getCalendarMonthGrid() : { cells: [], today: '' };
  const weekDays = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];

  return (
    <div className="max-w-2xl mx-auto px-4 md:px-8 py-6 md:py-10 pb-24 animate-in fade-in duration-500">
      
      {/* Progress Header */}
      <div className="mb-8">
        <div className="flex items-center gap-2 text-sm text-muted mb-4">
            <button onClick={handleBack} className="flex items-center gap-2 text-muted hover:text-primary transition-colors text-sm py-2">
                <ChevronLeft className="w-4 h-4" /> Précédent
            </button>
            <span>/</span>
            <span className="font-bold text-text">Réservation</span>
        </div>
        
        <h1 className="text-3xl font-black text-text mb-4">
            {currentStep === 0 && 'Choisissez votre soin'}
            {currentStep === 1 && 'Choisissez votre créneau'}
            {currentStep === 2 && 'Confirmation'}
        </h1>

        <div className="w-full bg-gray-200 h-2 rounded-full overflow-hidden">
            <div 
                className="bg-primary h-full transition-all duration-500 ease-out" 
                style={{ width: `${((currentStep + 1) / steps.length) * 100}%` }}
            />
        </div>
      </div>

      {/* Step 1: Services */}
      {currentStep === 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {services.map(service => (
                <div
                    key={service.id}
                    className={`group bg-white rounded-xl overflow-hidden border-2 transition-all hover:shadow-md min-h-[70px] ${
                        selectedService?.id === service.id ? 'border-primary ring-1 ring-primary' : 'border-transparent hover:border-gray-200'
                    }`}
                >
                    {/* Photo banner */}
                    <div
                        className="w-full h-40 bg-gray-100 overflow-hidden cursor-pointer relative"
                        onClick={() => setSelectedService(service)}
                    >
                        {servicePhotos[service.id] || service.image
                            ? <img
                                src={servicePhotos[service.id] || service.image}
                                alt={service.name}
                                className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                              />
                            : <div className="w-full h-full flex items-center justify-center text-5xl bg-gradient-to-br from-gray-50 to-gray-100">✨</div>
                        }
                        {selectedService?.id === service.id && (
                            <div className="absolute top-2 right-2 w-7 h-7 bg-primary rounded-full flex items-center justify-center text-white text-xs font-bold shadow">✓</div>
                        )}
                    </div>
                    {/* Card body */}
                    <div className="p-4">
                        <div className="flex justify-between items-start mb-1">
                            <h3
                                className="font-bold text-text text-base cursor-pointer hover:text-primary transition-colors"
                                onClick={() => setSelectedService(service)}
                            >
                                {service.name}
                            </h3>
                            <span className="text-primary font-bold text-sm shrink-0 ml-2">{service.price} DT</span>
                        </div>
                        <div className="flex items-center gap-1 text-xs text-muted font-bold uppercase tracking-wider mb-2">
                            <Clock className="w-3 h-3" /> {service.durationMinutes ?? (service as any).duration_min ?? 60} min
                        </div>
                        <p className="text-xs text-muted line-clamp-2 mb-3">{service.description}</p>
                        <div className="flex gap-2">
                            <button
                                onClick={() => setSelectedService(service)}
                                className={`flex-1 py-2 rounded-lg text-xs font-bold transition-all ${
                                    selectedService?.id === service.id
                                        ? 'bg-primary text-white'
                                        : 'bg-primary/10 text-primary hover:bg-primary/20'
                                }`}
                            >
                                {selectedService?.id === service.id ? '✓ Sélectionné' : 'Choisir'}
                            </button>
                            <button
                                onClick={() => setPreviewService(service)}
                                className="px-3 py-2 rounded-lg text-xs font-bold border border-gray-200 text-muted hover:border-primary hover:text-primary transition-all"
                            >
                                Voir plus
                            </button>
                        </div>
                    </div>
                </div>
            ))}
        </div>
      )}

      {/* Step 2: Calendar */}
      {currentStep === 1 && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
            {/* Calendar Widget - Month view */}
            <div className="lg:col-span-12 bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                <div className="flex justify-between items-center mb-4">
                    <h3 className="font-bold text-lg">Sélectionnez une date</h3>
                    <div className="flex items-center gap-2">
                      <button
                        type="button"
                        onClick={() => setCalendarMonth(prev => {
                          const p = new Date(prev);
                          p.setMonth(p.getMonth() - 1);
                          return p;
                        })}
                        className="w-9 h-9 rounded-lg flex items-center justify-center border border-gray-200 hover:bg-gray-50 transition-colors"
                        aria-label="Mois précédent"
                      >
                        <ChevronLeft className="w-5 h-5 text-gray-600" />
                      </button>
                      <span className="min-w-[160px] text-center font-bold text-text" style={{ fontFamily: 'Cormorant Garamond, serif', fontSize: '1.15rem' }}>
                        {calendarMonth.toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' })}
                      </span>
                      <button
                        type="button"
                        onClick={() => setCalendarMonth(prev => {
                          const p = new Date(prev);
                          p.setMonth(p.getMonth() + 1);
                          return p;
                        })}
                        className="w-9 h-9 rounded-lg flex items-center justify-center border border-gray-200 hover:bg-gray-50 transition-colors"
                        aria-label="Mois suivant"
                      >
                        <ChevronRight className="w-5 h-5 text-gray-600" />
                      </button>
                    </div>
                </div>
                <div className="grid grid-cols-7 gap-1 mb-2">
                  {weekDays.map(w => (
                    <div key={w} className="text-center text-xs font-bold uppercase text-muted py-1">
                      {w}
                    </div>
                  ))}
                </div>
                <div className="grid grid-cols-7 gap-1">
                  {calendarCells.map((cell, idx) => {
                    if (!cell.dateStr) {
                      return <div key={idx} className="aspect-square rounded-lg" />;
                    }
                    const isSelected = selectedDate === cell.dateStr;
                    const isPast = cell.dateStr < todayStr;
                    const hasSlots = dateSlotsMap[cell.dateStr] === true;
                    const noSlots = dateSlotsMap[cell.dateStr] === false;
                    const disabled = isPast || noSlots;
                    return (
                      <button
                        key={cell.dateStr}
                        type="button"
                        disabled={disabled}
                        onClick={() => { if (!disabled) { setSelectedDate(cell.dateStr!); setSelectedSlot(null); } }}
                        className={`aspect-square rounded-lg flex flex-col items-center justify-center text-sm transition-all border
                          ${isSelected
                            ? 'bg-primary text-white border-primary shadow-md font-bold'
                            : disabled
                              ? 'bg-gray-50 border-gray-100 text-gray-300 cursor-not-allowed opacity-60'
                              : hasSlots
                                ? 'bg-green-50 border-green-200 text-green-800 hover:border-green-400 hover:bg-green-100 font-semibold'
                                : 'bg-white border-gray-100 text-text hover:border-primary/50'
                          }`}
                      >
                        {cell.day}
                      </button>
                    );
                  })}
                </div>

                <div className="mt-8">
                    <h3 className="font-bold text-lg mb-4">Créneaux disponibles</h3>
                    {loading ? (
                        <div className="flex justify-center py-8"><Loader2 className="w-6 h-6 animate-spin text-primary" /></div>
                    ) : availableSlots.length > 0 ? (
                        <div className="grid grid-cols-3 gap-2">
                            {availableSlots.map((slot, idx) => {
                                const timeStr = new Date(slot.startAt).toLocaleTimeString('fr-FR', {hour: '2-digit', minute: '2-digit'});
                                const isSelected = selectedSlot?.startAt === slot.startAt;
                                return (
                                    <button
                                        key={idx}
                                        onClick={() => { setSelectedSlot(slot); setBookingError(''); }}
                                        className={`py-2.5 px-2 rounded-lg text-xs font-medium border transition-all ${
                                            isSelected
                                            ? 'border-primary bg-primary/10 text-primary' 
                                            : 'border-gray-100 hover:border-primary/50'
                                        }`}
                                        style={{ minHeight: '40px' }}
                                    >
                                        {timeStr}
                                    </button>
                                )
                            })}
                        </div>
                    ) : (
                        <p className="text-muted text-center py-4">Aucun créneau disponible pour cette date.</p>
                    )}
                </div>
            </div>
        </div>
      )}

      {/* Step 3: Confirmation Summary */}
      {currentStep === 2 && selectedService && selectedSlot && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              <div className="space-y-6">
                  <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                      <h3 className="font-bold text-lg mb-4">Récapitulatif</h3>
                      
                      <div className="space-y-4">
                          <div className="flex items-center gap-4">
                              <div className="w-12 h-12 bg-gray-100 rounded-lg overflow-hidden">
                                  {servicePhotos[selectedService.id] || selectedService.image
                                    ? <img
                                        src={servicePhotos[selectedService.id] || selectedService.image}
                                        className="w-full h-full object-cover"
                                      />
                                    : <div className="w-full h-full flex items-center justify-center text-xl bg-gray-50">✨</div>
                                  }
                              </div>
                              <div>
                                  <p className="text-sm font-bold text-text">{selectedService.name}</p>
                                  <p className="text-sm text-primary font-bold">{selectedService.price} DT • {selectedService.durationMinutes ?? (selectedService as any).duration_min ?? 60} min</p>
                              </div>
                          </div>

                           <hr className="border-gray-100" />

                           <div className="flex items-center gap-4">
                                <div className="w-12 h-12 bg-primary/10 rounded-lg flex items-center justify-center text-primary">
                                    <CalIcon className="w-6 h-6" />
                                </div>
                                <div>
                                    <p className="text-sm font-bold text-text">{new Date(selectedSlot.startAt).toLocaleDateString('fr-FR', {weekday: 'long', day: 'numeric', month: 'long'})}</p>
                                    <p className="text-sm text-muted">à {new Date(selectedSlot.startAt).toLocaleTimeString('fr-FR', {hour:'2-digit', minute: '2-digit'})}</p>
                                </div>
                           </div>
                      </div>
                  </div>

                  <div className="bg-blue-50 text-blue-800 p-4 rounded-xl flex items-start gap-3">
                      <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
                      <p className="text-sm">L'annulation est gratuite jusqu'à 24h avant le rendez-vous.</p>
                  </div>
              </div>

              <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100 h-fit">
                   <h3 className="font-bold text-lg mb-4">Total à payer</h3>
                   <div className="flex justify-between items-center mb-2">
                       <span className="text-muted text-sm">Sous-total</span>
                       <span className="font-semibold">{selectedService.price},00 DT</span>
                   </div>
                   <div className="flex justify-between items-center mb-6">
                       <span className="text-muted text-sm">TVA (19%)</span>
                       <span className="font-semibold">{(selectedService.price * 0.19).toFixed(2)} DT</span>
                   </div>
                   <div className="flex justify-between items-center mb-8 pt-4 border-t border-gray-100">
                       <span className="font-black text-lg">Total</span>
                       <span className="font-black text-xl text-primary">{(selectedService.price * 1.19).toFixed(2)} DT</span>
                   </div>

                   {bookingError && (
                     <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-xl text-sm text-red-700 font-medium">
                       ⚠️ {bookingError}
                     </div>
                   )}

                   <button 
                        onClick={handleNext}
                        disabled={loading} 
                        className="w-full py-4 bg-primary text-white rounded-xl font-bold shadow-lg shadow-primary/30 hover:bg-primary-dark transition-all flex items-center justify-center gap-2"
                    >
                       {loading ? <Loader2 className="animate-spin" /> : <>Confirmer la réservation <CheckCircle2 className="w-5 h-5" /></>}
                   </button>
              </div>
          </div>
      )}

      {/* Floating Action Bar */}
      {currentStep < 2 && (
        <div className="fixed bottom-0 left-0 w-full bg-white border-t border-gray-100 p-4 md:hidden z-30">
             <button 
                onClick={handleNext}
                disabled={!isStepValid()}
                className="w-full py-3 bg-primary text-white rounded-xl font-bold disabled:opacity-50 disabled:cursor-not-allowed"
             >
                Continuer
             </button>
        </div>
      )}

      {currentStep === 2 && selectedService && selectedSlot && (
        <div className="fixed bottom-0 left-0 w-full bg-white border-t border-gray-100 p-4 md:hidden z-30">
          <button
            onClick={handleNext}
            disabled={loading}
            className="w-full py-4 bg-primary text-white rounded-xl font-bold shadow-lg shadow-primary/30 hover:bg-primary-dark transition-all flex items-center justify-center gap-2"
          >
            {loading ? <Loader2 className="animate-spin" /> : <>Confirmer la réservation <CheckCircle2 className="w-5 h-5" /></>}
          </button>
        </div>
      )}
      
      {currentStep < 2 && (
        <div className="hidden md:flex justify-end mt-8">
            <button 
                onClick={handleNext}
                disabled={!isStepValid()}
                className="px-8 py-3 bg-primary text-white rounded-xl font-bold disabled:opacity-50 disabled:cursor-not-allowed hover:bg-primary-dark transition-all flex items-center gap-2"
             >
                Continuer <ChevronRight className="w-4 h-4" />
             </button>
        </div>
      )}

      {/* Service Preview Modal */}
      {previewService && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4" onClick={() => setPreviewService(null)}>
            <div
                className="bg-white rounded-2xl overflow-hidden max-w-lg w-full shadow-2xl"
                onClick={e => e.stopPropagation()}
            >
                {/* Photo */}
                <div className="w-full h-56 bg-gray-100 relative">
                    {servicePhotos[previewService.id] || previewService.image
                        ? <img
                            src={servicePhotos[previewService.id] || previewService.image}
                            alt={previewService.name}
                            className="w-full h-full object-cover"
                          />
                        : <div className="w-full h-full flex items-center justify-center text-6xl bg-gradient-to-br" style={{ background: 'linear-gradient(135deg,#FFF0E8,#F5D9C8)' }}>✨</div>
                    }
                    <button
                        onClick={() => setPreviewService(null)}
                        className="absolute top-3 right-3 w-8 h-8 bg-white/90 backdrop-blur rounded-full flex items-center justify-center text-gray-600 hover:text-gray-900 shadow"
                    >
                        ✕
                    </button>
                </div>
                {/* Content */}
                <div className="p-6">
                    <div className="flex justify-between items-start mb-3">
                        <h2 className="text-xl font-black text-text">{previewService.name}</h2>
                        <span className="text-lg font-black text-primary">{previewService.price} DT</span>
                    </div>
                    <div className="flex items-center gap-4 mb-4">
                        <div className="flex items-center gap-1.5 text-sm text-muted font-semibold">
                            <Clock className="w-4 h-4" />
                            {previewService.durationMinutes ?? (previewService as any).duration_min ?? 60} min
                        </div>
                        <div className="w-1 h-1 rounded-full bg-gray-300" />
                        <span className="text-xs font-bold px-2 py-1 rounded-full" style={{ backgroundColor: '#F0F7EE', color: '#3B6D11' }}>
                            Disponible
                        </span>
                    </div>
                    {previewService.description && (
                        <p className="text-sm text-muted leading-relaxed mb-6">{previewService.description}</p>
                    )}
                    <div className="flex gap-3">
                        <button
                            onClick={() => setPreviewService(null)}
                            className="flex-1 py-3 rounded-xl text-sm font-bold border border-gray-200 text-muted hover:bg-gray-50 transition-all"
                        >
                            Fermer
                        </button>
                        <button
                            onClick={() => {
                                setSelectedService(previewService);
                                setPreviewService(null);
                            }}
                            className="flex-1 py-3 rounded-xl text-sm font-bold text-white transition-all"
                            style={{ backgroundColor: '#A17969' }}
                        >
                            {selectedService?.id === previewService.id ? '✓ Déjà sélectionné' : 'Choisir ce soin'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
      )}

    </div>
  );
}