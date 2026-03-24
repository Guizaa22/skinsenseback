import React, { useState, useEffect } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Calendar, User, FileText, Settings, LogOut, Menu, X, PlusCircle, Bell } from 'lucide-react';
import { api } from '@/src/services/api.ts';

export default function Layout() {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [showNotifs, setShowNotifs] = useState(false);
  const [notifs, setNotifs] = useState<any[]>([]);
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    api.appointments.list()
      .then(res => {
        const data = res?.data ?? [];
        const upcoming = Array.isArray(data)
          ? data.filter((a: any) => a.status === 'CONFIRMED' || a.status === 'PENDING')
          : [];
        setNotifs(upcoming);
      })
      .catch(() => {});
  }, []);

  const handleBellClick = async () => {
    setShowNotifs(!showNotifs);
    if (!showNotifs) {
      try {
        const res = await api.appointments.list();
        const upcoming = (res.data ?? []).filter(
          (a: any) => a.status === 'CONFIRMED' || a.status === 'PENDING'
        );
        setNotifs(upcoming);
      } catch {
        setNotifs([]);
      }
    }
  };

  const handleLogout = () => {
    // In a real app, clear tokens here
    navigate('/login');
  };

  const NavItem = ({ to, icon: Icon, label }: { to: string; icon: any; label: string }) => {
    const isActive = location.pathname === to;
    return (
      <NavLink
        to={to}
        onClick={() => setIsMobileMenuOpen(false)}
        className="flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 group"
        style={isActive ? { backgroundColor: '#EBCDC4', color: '#A17969', fontWeight: 700 } : { color: '#736357' }}
      >
        <Icon className="w-5 h-5" style={isActive ? { color: '#A17969' } : { color: '#736357' }} />
        <span className="text-sm">{label}</span>
      </NavLink>
    );
  };

  return (
    <div className="flex h-screen overflow-hidden" style={{ backgroundColor: '#F5EDE8' }}>
      {isMobileMenuOpen && (
        <div
          className="lg:hidden fixed inset-0 bg-black/40 z-40"
          onClick={() => setIsMobileMenuOpen(false)}
        />
      )}
      {/* Sidebar - Desktop */}
      <aside
        className={`w-72 flex flex-col border-r h-full fixed top-0 left-0 z-50 transform transition-transform duration-300 lg:relative lg:transform-none lg:translate-x-0 ${
          isMobileMenuOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
        style={{ backgroundColor: '#F5EDE8', borderColor: '#E9DCD6' }}
      >
        <div className="p-8">
          <div className="flex flex-col items-center mb-8 cursor-pointer" onClick={() => navigate('/app')}>
            <img
              src="/skinsense-logo-full.png"
              alt="SkinSense"
              style={{ width: '140px', marginBottom: '8px' }}
            />
            <p style={{ color: '#998675', fontSize: '10px', letterSpacing: '0.15em', textTransform: 'uppercase' }}>
              Espace Client Privé
            </p>
          </div>

          <nav className="space-y-2">
            <NavItem to="/app" icon={Calendar} label="Mes Rendez-vous" />
            <NavItem to="/app/client-file" icon={FileText} label="Mon Dossier" />
            <NavItem to="/app/history" icon={User} label="Historique" />
            <NavItem to="/app/settings" icon={Settings} label="Paramètres" />
          </nav>
        </div>

        <div className="mt-auto p-8">
          <div className="rounded-xl p-4 mb-6" style={{ backgroundColor: 'rgba(235,205,196,0.5)', border: '1px solid rgba(161,121,105,0.2)' }}>
            <p className="text-xs font-bold uppercase tracking-wider mb-1" style={{ color: '#A17969' }}>Programme Fidélité</p>
            <p className="text-sm font-bold mb-2" style={{ color: '#534741' }}>1,250 Points</p>
            <div className="w-full h-1.5 rounded-full" style={{ backgroundColor: '#E9DCD6' }}>
              <div className="h-1.5 rounded-full" style={{ width: '65%', backgroundColor: '#A17969' }}></div>
            </div>
          </div>
          
          <button 
            onClick={handleLogout}
            className="flex items-center gap-3 text-sm font-bold transition-colors w-full px-4 hover:opacity-80"
            style={{ color: '#736357' }}
          >
            <LogOut className="w-5 h-5" />
            Déconnexion
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col h-full overflow-hidden relative">
        {/* Header - Mobile & Desktop */}
        <header className="bg-white z-10 shadow-sm" style={{ borderBottom: '1px solid #E9DCD6' }}>
          <div className="max-w-6xl mx-auto px-6 py-4 flex justify-between items-center">
            <div className="flex items-center gap-2 md:gap-3">
              <button className="lg:hidden shrink-0" onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}>
                {isMobileMenuOpen ? <X style={{ color: '#534741' }} /> : <Menu style={{ color: '#534741' }} />}
              </button>
              <img src="/skinsense-logo-text.png" alt="SkinSense" style={{ height: '24px' }} />
            </div>
            
            <div className="hidden lg:block">
               {/* Breadcrumbs or greeting could go here */}
            </div>

            <div className="flex items-center gap-4">
               <button 
                onClick={() => navigate('/app/booking')}
                className="flex items-center gap-2 bg-primary text-white px-3 py-2 md:px-5 md:py-2.5 rounded-xl text-xs md:text-sm font-bold transition-all shadow-md hover:bg-[#998675]"
                style={{ backgroundColor: '#A17969' }}
               >
                 <PlusCircle className="w-4 h-4" />
                 Prendre RDV
               </button>

               <div className="relative">
                 <button
                   onClick={handleBellClick}
                   className="relative w-10 h-10 rounded-full flex items-center justify-center transition-colors"
                   style={{ backgroundColor: '#E9DCD6', color: '#736357' }}
                   aria-label="Notifications"
                 >
                   <Bell className="w-5 h-5" />
                   {notifs.length > 0 && (
                     <span className="absolute -top-1 -right-1 text-white text-xs rounded-full w-4 h-4 flex items-center justify-center font-bold" style={{ backgroundColor: '#A17969' }}>
                       {notifs.length > 9 ? '9+' : notifs.length}
                     </span>
                   )}
                 </button>
                 {showNotifs && (
                   <div
                     className="absolute right-0 top-12 w-80 max-w-[calc(100vw-2rem)] bg-white rounded-2xl shadow-xl border border-gray-100 z-50 p-4"
                     style={{ right: 0, maxWidth: 'calc(100vw - 2rem)' }}
                   >
                     <h3 className="font-bold text-sm mb-3">Mes rendez-vous à venir</h3>
                     {notifs.length === 0 ? (
                       <p className="text-muted text-sm">Aucun rendez-vous à venir</p>
                     ) : (
                       notifs.slice(0, 5).map((n: any) => (
                         <div key={n.id} className="py-3 border-b border-gray-50 last:border-0">
                           <div className="flex items-center justify-between mb-1">
                             <p className="text-sm font-medium">{n.serviceName ?? 'Rendez-vous'}</p>
                             <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                               n.status === 'CONFIRMED'
                                 ? 'bg-green-100 text-green-700'
                                 : n.status === 'PENDING'
                                 ? 'bg-yellow-100 text-yellow-700'
                                 : 'bg-gray-100 text-gray-600'
                             }`}>
                               {n.status === 'CONFIRMED' ? '✓ Confirmé' :
                                n.status === 'PENDING' ? '⏳ En attente' : n.status}
                             </span>
                           </div>
                           <p className="text-xs text-muted">
                             📅 {new Date(n.startAt).toLocaleDateString('fr-FR', {
                               weekday: 'long', day: 'numeric', month: 'long'
                             })}
                           </p>
                           <p className="text-xs text-muted">
                             🕐 {new Date(n.startAt).toLocaleTimeString('fr-FR', {
                               hour: '2-digit', minute: '2-digit'
                             })}
                           </p>
                         </div>
                       ))
                     )}
                     <button
                       onClick={handleBellClick}
                       className="w-full text-xs text-primary font-medium pt-3 hover:underline"
                     >
                       🔄 Actualiser
                     </button>
                   </div>
                 )}
               </div>
               
              <div
                className="w-10 h-10 rounded-full overflow-hidden border-2 shadow-sm flex items-center justify-center"
                style={{ borderColor: '#CEAA9A', backgroundColor: '#F5EDE8' }}
              >
                <img
                  src="/skinsense-logo-emblem.png"
                  alt="SkinSense"
                  style={{ width: '28px', height: '28px', objectFit: 'contain' }}
                />
              </div>
            </div>
          </div>
        </header>

        {/* Scrollable Content Area */}
        <main className="flex-1 overflow-y-auto bg-background p-4 md:p-8 scroll-smooth" style={{ backgroundColor: '#F5EDE8' }}>
          <div className="max-w-6xl mx-auto pb-20">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}