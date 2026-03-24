import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRight, Star, MapPin, Phone, Instagram, Facebook, ChevronDown } from 'lucide-react';
import { api, fetchSecureImage } from '@/src/services/api.ts';

const fullText = 'Skinsense';
const GOLD = '#C6976D';
const GOLD_DARK = '#A17969';

export default function Landing() {
  const navigate = useNavigate();
  const [scrollY, setScrollY] = useState(0);
  const [typed, setTyped] = useState('');
  const [services, setServices] = useState<any[]>([]);
  const [employees, setEmployees] = useState<any[]>([]);
  const [servicePhotos, setServicePhotos] = useState<Record<string, string>>({});
  const [servicesBlockInView, setServicesBlockInView] = useState(false);
  const servicesSectionRef = useRef<HTMLElement>(null);

  useEffect(() => {
    const handleScroll = () => setScrollY(window.scrollY);
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  useEffect(() => {
    const base = ((import.meta as any).env?.VITE_API_BASE_URL ?? '').replace(/\/+$/, '');
    const headers = { 'Content-Type': 'application/json' };
    Promise.all([
      fetch(`${base}/api/services`, { headers }).then(r => r.json()).catch(() => ({ data: [] })),
      fetch(`${base}/api/scheduling/employees`, { headers }).then(r => r.json()).catch(() => ({ data: [] })),
    ]).then(([svcRes, empRes]) => {
      const svcs = Array.isArray(svcRes?.data) ? svcRes.data : [];
      const emps = Array.isArray(empRes?.data) ? empRes.data : [];
      setServices(svcs.filter((s: any) => s.isActive !== false).slice(0, 4));
      setEmployees(emps.slice(0, 6));
      svcs.filter((s: any) => s.isActive !== false).slice(0, 4).forEach((svc: any) => {
        if (!svc.id) return;
        // Use the same secure blob approach as Booking page
        api.photos
          .getServicePhotos(svc.id)
          .then((res: any) => {
            const photos = Array.isArray(res?.data) ? res.data : [];
            if (photos.length > 0 && photos[0]?.url) {
              fetchSecureImage(photos[0].url).then(blobUrl => {
                if (blobUrl) {
                  setServicePhotos(prev => ({
                    ...prev,
                    [svc.id]: blobUrl,
                  }));
                }
              });
            }
          })
          .catch(() => {});
      });
    });
  }, []);

  useEffect(() => {
    let i = 0;
    const timer = setInterval(() => {
      if (i <= fullText.length) {
        setTyped(fullText.slice(0, i));
        i++;
      } else clearInterval(timer);
    }, 120);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    const el = servicesSectionRef.current;
    if (!el) return;
    const obs = new IntersectionObserver(
      ([e]) => setServicesBlockInView(e.isIntersecting),
      { threshold: 0.15, rootMargin: '0px' }
    );
    obs.observe(el);
    return () => obs.disconnect();
  }, []);

  const scrollProgress = Math.min(scrollY / 600, 1);
  const fontSize = 4 + scrollProgress * 10;
  const opacity = scrollProgress > 0.7 ? 1 - (scrollProgress - 0.7) / 0.3 : 1;
  const bgOpacity = scrollProgress * 0.85;

  return (
    <div className="bg-background min-h-screen font-sans text-text">
      <style>{`@keyframes pulse { 0%,100%{opacity:0.3} 50%{opacity:1} }`}</style>
       {/* Navigation */}
       <nav className="fixed w-full z-50 border-b" style={{ background: 'rgba(255,248,244,0.92)', backdropFilter: 'blur(12px)', borderColor: '#EDD9CE' }}>
         <div className="max-w-7xl mx-auto px-6 h-20 flex justify-between items-center">
           <div className="flex items-center gap-2">
             <img src="/logo-circle-gold.png" alt="" className="h-10 w-10 object-contain shrink-0" />
             <span className="text-xl font-extrabold" style={{ color: '#7C4F36', fontFamily: "'Cormorant Garamond', serif", letterSpacing: '0.05em' }}>SkinSense</span>
           </div>
           <div className="flex items-center gap-6">
             <button onClick={() => navigate('/login')} className="font-semibold hidden sm:block transition-colors" style={{ color: '#A17060' }}>Connexion</button>
             <button onClick={() => navigate('/login')} className="px-6 py-2.5 rounded-xl font-bold text-white transition-all" style={{ background: 'linear-gradient(135deg,#C6976D,#A17060)', boxShadow: '0 4px 16px rgba(161,112,96,0.3)' }}>
               Prendre RDV
             </button>
           </div>
         </div>
       </nav>

       {/* Hero - Animated */}
       <header className="relative min-h-screen flex flex-col items-center justify-center overflow-hidden pt-20">
         {/* Animated background that fills on scroll */}
         <div
           className="absolute inset-0 z-0"
           style={{
             background: 'linear-gradient(160deg, #FFF0E8 0%, #F5D9C8 50%, #E8C4A8 100%)',
             opacity: bgOpacity,
             transition: 'opacity 0.1s ease-out',
           }}
         />

         {/* Skinsense 3D / expanding animation in centre (no circle photo) */}
         <div
           className="relative z-10 flex flex-col items-center justify-center text-center px-6"
           style={{
             fontFamily: "'Cormorant Garamond', serif",
             fontSize: `${fontSize}rem`,
             fontWeight: 300,
             opacity,
             color: scrollProgress > 0.3 ? '#FFF8F4' : '#7C4F36',
             letterSpacing: '0.12em',
             lineHeight: 1,
             transition: 'color 0.3s',
             textShadow: scrollProgress > 0.3 ? '0 0 80px rgba(199,178,153,0.3)' : 'none',
           }}
         >
           {typed}
           <span style={{ animation: 'pulse 1s ease-in-out infinite' }}>|</span>
           {typed === fullText && (
             <div className="mt-8 flex flex-col items-center gap-6">
               <p className="text-lg tracking-[0.2em] font-light" style={{ color: '#A17060', fontFamily: "'Cormorant Garamond', serif" }}>
                 Centre de Beauté & Soins · Tunis
               </p>
               <div className="flex flex-wrap items-center justify-center gap-4">
                 <button
                   onClick={() => navigate('/login')}
                   className="px-8 py-3 rounded-full text-sm font-semibold text-white transition-all hover:opacity-90"
                   style={{ backgroundColor: '#A17969', letterSpacing: '0.05em' }}
                 >
                   Prendre RDV
                 </button>
                 <button
                   onClick={() => window.scrollTo({ top: 800, behavior: 'smooth' })}
                   className="px-8 py-3 rounded-full text-sm font-semibold transition-all hover:opacity-90"
                   style={{ border: '1px solid #CEAA9A', color: '#736357', backgroundColor: 'transparent', letterSpacing: '0.05em' }}
                 >
                   Découvrir
                 </button>
               </div>
             </div>
           )}
         </div>

         {/* Scroll indicator */}
         <div className="absolute bottom-8 left-1/2 -translate-x-1/2 z-10 flex flex-col items-center gap-2" style={{ opacity: 1 - scrollProgress }}>
           <ChevronDown className="w-6 h-6" style={{ color: '#736357', animation: 'pulse 2s ease-in-out infinite' }} />
           <span className="text-xs font-medium tracking-widest uppercase" style={{ color: '#736357' }}>Défiler</span>
         </div>
       </header>

       {/* Services Grid - gold label, scroll-in and hover effects */}
       <section
         id="services"
         ref={servicesSectionRef}
         className="py-24 bg-white transition-all duration-700"
         style={{
           backgroundColor: servicesBlockInView ? 'rgba(255,248,244,0.4)' : undefined,
           borderTop: servicesBlockInView ? `2px solid ${GOLD}` : undefined,
           borderBottom: servicesBlockInView ? `2px solid ${GOLD}` : undefined,
         }}
       >
        <div className="max-w-7xl mx-auto px-6">
            <div className="text-center mb-16">
                <span
                  className="font-bold tracking-wider uppercase text-sm transition-colors duration-500"
                  style={{ color: servicesBlockInView ? GOLD_DARK : GOLD }}
                >
                  Nos Prestations
                </span>
                <h2 className="text-4xl font-black text-text mt-2 mb-4 transition-colors duration-300 group-hover:text-inherit">Une carte de soins sur-mesure</h2>
                <p className="text-muted max-w-2xl mx-auto">Des soins du visage haute technologie aux massages relaxants, découvrez notre sélection.</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
                {services.map(service => (
                    <div key={service.id} className="group cursor-pointer rounded-2xl p-1 transition-all duration-300 hover:shadow-lg hover:shadow-[#C6976D]/20" onClick={() => navigate('/login')}>
                        <div className="rounded-2xl overflow-hidden aspect-[4/3] mb-4 relative border-2 border-transparent group-hover:border-[#C6976D]/40 transition-colors duration-300">
                            {servicePhotos[service.id] || service.image
                              ? <img
                                  src={servicePhotos[service.id] || service.image}
                                  alt={service.name}
                                  className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                                />
                              : <div className="w-full h-full flex items-center justify-center text-4xl" style={{ background: 'linear-gradient(135deg,#FFF0E8,#F5D9C8)' }}>✨</div>
                            }
                            <div className="absolute top-3 right-3 bg-white/90 backdrop-blur px-3 py-1 rounded-full text-sm font-bold shadow-sm">
                                {service.price} DT
                            </div>
                        </div>
                        <h3 className="text-xl font-bold text-text mb-2 transition-colors duration-300 group-hover:text-[#A17969]">{service.name}</h3>
                        <p className="text-muted text-sm line-clamp-2">{service.description}</p>
                        <div className="mt-4 flex items-center text-sm font-bold text-[#C6976D] group-hover:text-[#A17969] transition-colors duration-300">
                            En savoir plus <ArrowRight className="w-4 h-4 ml-1 transition-transform group-hover:translate-x-1 inline-block" style={{ color: 'inherit' }} />
                        </div>
                    </div>
                ))}
            </div>
        </div>
       </section>

       {/* Team */}
        <section className="py-24 bg-background">
            <div className="max-w-7xl mx-auto px-6">
                <div className="flex flex-col md:flex-row justify-between items-end mb-12 gap-6">
                    <div>
                        <span className="text-primary font-bold tracking-wider uppercase text-sm">Nos Experts</span>
                        <h2 className="text-4xl font-black text-text mt-2">Rencontrez l'équipe</h2>
                    </div>
                    <button onClick={() => navigate('/login')} className="px-6 py-3 bg-white border border-gray-200 rounded-xl font-bold text-sm hover:bg-gray-50 transition-all">
                        Prendre rendez-vous avec un expert
                    </button>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
                    {employees.map(employee => (
                        <div key={employee.id} className="bg-white p-4 rounded-2xl shadow-sm border border-gray-100 flex items-center gap-4">
                            <div className="w-20 h-20 rounded-xl overflow-hidden shrink-0 flex items-center justify-center font-bold text-white text-xl" style={{ background: 'linear-gradient(135deg,#C6976D,#A17060)' }}>
                              {employee.image
                                ? <img src={employee.image} alt={employee.fullName ?? employee.name} className="w-full h-full object-cover" />
                                : (employee.fullName ?? employee.name ?? '?').slice(0, 2).toUpperCase()
                              }
                            </div>
                            <div>
                                <h3 className="font-bold text-lg" style={{ color: '#534741' }}>{employee.fullName ?? employee.name ?? '—'}</h3>
                                <p className="font-medium text-sm mb-1" style={{ color: '#A17060' }}>{employee.jobTitle ?? employee.specialties?.length ? `${employee.specialties?.length ?? 0} spécialité(s)` : 'Expert'}</p>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </section>

       {/* Footer */}
       <footer className="pt-20 pb-10 border-t border-gray-100" style={{ background: '#FFF8F4' }}>
         <div className="max-w-7xl mx-auto px-6">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-12 mb-16">
                <div className="col-span-1 md:col-span-2">
                                        <div className="flex items-center mb-6">
                      <img src="/logo-combined-gold.png" alt="SkinSense" style={{ height: '48px', objectFit: 'contain' }} />
                    </div>
                    <p className="text-muted max-w-sm mb-6">
                        Votre destination beauté de référence. Nous nous engageons à vous offrir des soins d'excellence dans un cadre apaisant et luxueux.
                    </p>
                    <div className="flex gap-4">
                        <a href="#" className="w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center text-muted hover:bg-primary hover:text-white transition-all"><Instagram className="w-5 h-5"/></a>
                        <a href="#" className="w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center text-muted hover:bg-primary hover:text-white transition-all"><Facebook className="w-5 h-5"/></a>
                    </div>
                </div>
                
                <div>
                    <h4 className="font-bold text-text mb-6">Contact</h4>
                    <ul className="space-y-4 text-muted text-sm">
                        <li className="flex items-start gap-3">
                            <MapPin className="w-5 h-5 text-primary shrink-0" />
                            <span>15 Avenue de la République,<br/>Les Berges du Lac 2, Tunis</span>
                        </li>
                        <li className="flex items-center gap-3">
                            <Phone className="w-5 h-5 text-primary shrink-0" />
                            <span>+216 71 123 456</span>
                        </li>
                    </ul>
                </div>

                <div>
                    <h4 className="font-bold text-text mb-6">Horaires</h4>
                    <ul className="space-y-2 text-muted text-sm">
                        <li className="flex justify-between"><span>Lundi - Vendredi</span> <span className="font-bold text-text">09:00 - 19:00</span></li>
                        <li className="flex justify-between"><span>Samedi</span> <span className="font-bold text-text">09:00 - 17:00</span></li>
                        <li className="flex justify-between"><span>Dimanche</span> <span className="text-primary">Fermé</span></li>
                    </ul>
                </div>
            </div>
            
            <div className="border-t border-gray-100 pt-8 text-center text-sm text-muted">
                <p>&copy; 2025 Skinsense — Centre de Beauté & Soins. Tous droits réservés.</p>
            </div>
         </div>
       </footer>
    </div>
  );
}