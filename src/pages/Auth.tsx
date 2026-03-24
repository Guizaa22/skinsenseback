import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Mail, Lock, CheckCircle, Loader2 } from 'lucide-react';
import { useAuth } from '@/src/context/AuthContext.tsx';
import { api } from '@/src/services/api.ts';

export default function Auth() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [isLogin, setIsLogin] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [formData, setFormData] = useState({
      firstName: '',
      lastName: '',
      email: 'sophie.dubois@gmail.com', // Pre-fill for demo
      password: 'password'
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!isLogin) {
      const pwd = formData.password;
      if (pwd.length < 8 || !/[a-zA-Z]/.test(pwd) || !/\d/.test(pwd)) {
        setError('Le mot de passe doit contenir au moins 8 caractères, une lettre et un chiffre.');
        return;
      }
    }

    setLoading(true);
    try {
      if (isLogin) {
        const res = await api.auth.login(formData.email, formData.password);
        const accessToken = res.data.accessToken;
        localStorage.setItem('token', accessToken);
        const userRes = await api.auth.me();
        const user = { ...userRes.data, isActive: userRes.data.active };
        login(accessToken, user);
        navigate('/app');
      } else {
        const res = await api.auth.register(formData);
        const accessToken = res.data.accessToken;
        localStorage.setItem('token', accessToken);
        const userRes = await api.auth.me();
        const user = { ...userRes.data, isActive: userRes.data.active };
        login(accessToken, user);
        navigate('/app');
      }
    } catch (err: any) {
      setError(err.message || 'Une erreur est survenue. Vérifiez vos identifiants.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col md:flex-row" style={{ backgroundColor: '#F5EDE8' }}>
      {/* Visual Side */}
      <div
        className="hidden md:flex w-1/2 relative overflow-hidden"
        style={{ background: 'linear-gradient(135deg, #534741 0%, #736357 50%, #998675 100%)' }}
      >
        <div className="relative z-10 flex flex-col justify-center items-center p-16 text-white w-full">
          <div
            className="w-full max-w-[520px] text-center"
            style={{
              backgroundColor: 'rgba(255,255,255,0.94)',
              borderRadius: '26px',
              padding: '2.25rem 2.75rem',
              boxShadow: '0 24px 70px rgba(0,0,0,0.18)',
            }}
          >
            <img
              src="/logo-circle-gold.png"
              alt="SkinSense"
              style={{
                width: '280px',
                maxWidth: '100%',
                margin: '0 auto 1.25rem',
                objectFit: 'contain',
              }}
            />
            <p
              className="text-lg font-semibold tracking-widest mb-3"
              style={{ fontFamily: "'Cormorant Garamond', serif", color: '#A17969' }}
            >
              Beauty & Care Center
            </p>
            <p
              className="text-2xl font-light max-w-sm mx-auto"
              style={{ fontFamily: "'Cormorant Garamond', serif", color: '#A17969' }}
            >
              Votre parenthèse de sérénité
            </p>
          </div>
          <div className="flex gap-4 mt-12">
            <div className="flex items-center gap-2 px-4 py-2 rounded-full" style={{ background: 'rgba(255,255,255,0.1)', border: '1px solid rgba(199,178,153,0.3)' }}>
              <CheckCircle className="w-4 h-4" />
              <span className="text-sm font-bold">Soins Experts</span>
            </div>
            <div className="flex items-center gap-2 px-4 py-2 rounded-full" style={{ background: 'rgba(255,255,255,0.1)', border: '1px solid rgba(199,178,153,0.3)' }}>
              <CheckCircle className="w-4 h-4" />
              <span className="text-sm font-bold">Réservation 24/7</span>
            </div>
          </div>
        </div>
      </div>

      {/* Form Side */}
      <div className="flex flex-1 items-center justify-center p-4 md:p-12 w-full md:w-1/2">
        <div className="w-full max-w-[420px] p-6 md:p-8 rounded-3xl shadow-xl border" style={{ backgroundColor: '#FFFFFF', borderColor: '#E9DCD6' }}>
           <div className="md:hidden flex justify-center mb-6">
             <img src="/logo-circle-gold.png" alt="SkinSense" className="h-20 w-20 object-contain" />
           </div>
           <div className="text-center mb-8">
               <h2 className="text-2xl font-black mb-2" style={{ fontFamily: "'Cormorant Garamond', serif", color: '#534741' }}>Bienvenue</h2>
               <p className="text-sm" style={{ color: '#998675' }}>Accédez à votre espace client privilégié.</p>
           </div>

           {/* Tabs */}
           <div className="flex p-1 rounded-xl mb-8" style={{ backgroundColor: '#F5EDE8' }}>
               <button 
                className={`flex-1 py-2.5 text-sm font-bold rounded-lg transition-all ${isLogin ? 'text-white' : ''}`}
                style={isLogin ? { backgroundColor: '#A17969' } : { backgroundColor: 'transparent', color: '#998675' }}
                onClick={() => setIsLogin(true)}
               >
                   Connexion
               </button>
               <button 
                className={`flex-1 py-2.5 text-sm font-bold rounded-lg transition-all ${!isLogin ? 'text-white' : ''}`}
                style={!isLogin ? { backgroundColor: '#A17969' } : { backgroundColor: 'transparent', color: '#998675' }}
                onClick={() => setIsLogin(false)}
               >
                   S'inscrire
               </button>
           </div>

           <form onSubmit={handleSubmit} className="space-y-4">
               {!isLogin && (
                   <div className="grid grid-cols-2 gap-4">
                       <div>
                           <label className="text-xs font-bold mb-1 block" style={{ color: '#534741' }}>Prénom</label>
                           <input 
                            type="text" 
                            className="w-full px-4 py-3 rounded-xl border text-sm outline-none focus:ring-1 focus:ring-[#A17969] focus:border-[#A17969]"
                            style={{ backgroundColor: '#FDF8F5', border: '1px solid #E9DCD6', color: '#534741' }}
                            required 
                            value={formData.firstName}
                            onChange={e => setFormData({...formData, firstName: e.target.value})}
                           />
                       </div>
                       <div>
                           <label className="text-xs font-bold mb-1 block" style={{ color: '#534741' }}>Nom</label>
                           <input 
                            type="text" 
                            className="w-full px-4 py-3 rounded-xl border text-sm outline-none focus:ring-1 focus:ring-[#A17969] focus:border-[#A17969]"
                            style={{ backgroundColor: '#FDF8F5', border: '1px solid #E9DCD6', color: '#534741' }}
                            required 
                            value={formData.lastName}
                            onChange={e => setFormData({...formData, lastName: e.target.value})}
                           />
                       </div>
                   </div>
               )}
               
               <div>
                   <label className="text-xs font-bold mb-1 block" style={{ color: '#534741' }}>Adresse e-mail</label>
                   <div className="relative">
                       <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5" style={{ color: '#998675' }} />
                       <input 
                        type="email" 
                        placeholder="nom@exemple.com" 
                        className="w-full pl-12 pr-4 py-3 rounded-xl border text-sm outline-none focus:ring-1 focus:ring-[#A17969] focus:border-[#A17969]"
                        style={{ backgroundColor: '#FDF8F5', border: '1px solid #E9DCD6', color: '#534741' }}
                        required 
                        value={formData.email}
                        onChange={e => setFormData({...formData, email: e.target.value})}
                       />
                   </div>
               </div>

               <div>
                   <label className="text-xs font-bold mb-1 block" style={{ color: '#534741' }}>Mot de passe</label>
                   <div className="relative">
                       <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5" style={{ color: '#998675' }} />
                       <input 
                        type="password" 
                        placeholder="••••••••" 
                        className="w-full pl-12 pr-4 py-3 rounded-xl border text-sm outline-none focus:ring-1 focus:ring-[#A17969] focus:border-[#A17969]"
                        style={{ backgroundColor: '#FDF8F5', border: '1px solid #E9DCD6', color: '#534741' }}
                        required 
                        value={formData.password}
                        onChange={e => setFormData({...formData, password: e.target.value})}
                       />
                   </div>
                   {isLogin && <div className="text-right mt-2"><a href="#" className="text-xs font-bold hover:underline" style={{ color: '#A17969' }}>Mot de passe oublié ?</a></div>}
               </div>

                {error && <p className="text-red-500 text-sm font-bold text-center">{error}</p>}

               <button
                 disabled={loading}
                 className="w-full text-white font-bold py-4 rounded-xl shadow-lg transition-all mt-4 flex items-center justify-center disabled:opacity-70 hover:bg-[#998675]"
                 style={{ backgroundColor: '#A17969', boxShadow: '0 10px 15px -3px rgba(161,121,105,0.3)' }}
               >
                   {loading ? <Loader2 className="w-5 h-5 animate-spin" /> : (isLogin ? 'Se connecter' : 'Créer un compte')}
               </button>
           </form>

           <div className="mt-8 pt-8 text-center" style={{ borderTop: '1px solid #E9DCD6' }}>
               <p className="text-xs mb-4" style={{ color: '#998675' }}>
                   Ou continuer avec
               </p>
               <div className="flex gap-4">
                   <button className="flex-1 py-2.5 border rounded-xl hover:opacity-90 transition-colors flex items-center justify-center gap-2 text-sm font-semibold" style={{ borderColor: '#E9DCD6', color: '#534741' }}>
                       <img src="https://www.svgrepo.com/show/475656/google-color.svg" className="w-5 h-5" alt="Google" />
                       Google
                   </button>
                   <button className="flex-1 py-2.5 border rounded-xl hover:opacity-90 transition-colors flex items-center justify-center gap-2 text-sm font-semibold" style={{ borderColor: '#E9DCD6', color: '#534741' }}>
                       <img src="https://www.svgrepo.com/show/475633/apple-color.svg" className="w-5 h-5" alt="Apple" />
                       Apple
                   </button>
               </div>
           </div>
        </div>
      </div>
    </div>
  );
}