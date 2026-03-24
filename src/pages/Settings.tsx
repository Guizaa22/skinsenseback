import React, { useState } from 'react';
import { Bell, Smartphone, Mail } from 'lucide-react';
import { useAuth } from '@/src/context/AuthContext.tsx';
import { api } from '@/src/services/api.ts';

export default function Settings() {
  const { user } = useAuth();
  const [preferences, setPreferences] = useState({
    email: true,
    sms: true,
    reminderTime: '24'
  });
  const [saved, setSaved] = useState(false);
  const [saveError, setSaveError] = useState('');

  const handleSave = async () => {
    setSaveError('');
    if (!user) return;
    try {
      try {
        await api.users.updateProfile({
          fullName: user.fullName,
          phone: user.phone ?? '',
        });
      } catch (err: any) {
        if (err?.message?.includes('404')) {
          await api.users.updateProfileById(user.id, {
            fullName: user.fullName,
            phone: user.phone ?? '',
          });
        } else throw err;
      }
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch (err: any) {
      setSaveError(err?.message || 'Erreur lors de la sauvegarde');
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6 animate-in fade-in duration-500">
      <h1 className="text-3xl font-black text-text mb-2">Préférences de Notifications</h1>
      <p className="text-muted mb-8">Gérez comment vous souhaitez être informé de vos rendez-vous.</p>

      {saved && (
        <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-xl text-sm font-medium">
          ✓ Modifications enregistrées avec succès
        </div>
      )}
      {saveError && (
        <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-xl text-sm font-medium">
          {saveError}
        </div>
      )}

      <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden shadow-sm">
        <h2 className="text-lg font-bold p-6 border-b border-gray-100">Canaux de communication</h2>
        
        <div className="p-6 space-y-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center text-primary">
                <Mail className="w-6 h-6" />
              </div>
              <div>
                <p className="font-bold text-text">Notifications par Email</p>
                <p className="text-xs text-muted">Recevez vos confirmations et factures.</p>
              </div>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" checked={preferences.email} onChange={() => setPreferences(p => ({...p, email: !p.email}))} className="sr-only peer" />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
            </label>
          </div>

          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center text-primary">
                <Smartphone className="w-6 h-6" />
              </div>
              <div>
                <p className="font-bold text-text">Notifications par SMS</p>
                <p className="text-xs text-muted">Rappels rapides et offres exclusives.</p>
              </div>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" checked={preferences.sms} onChange={() => setPreferences(p => ({...p, sms: !p.sms}))} className="sr-only peer" />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
            </label>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden shadow-sm p-6">
          <h2 className="text-lg font-bold mb-4">Rappels de rendez-vous</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                  <label className="text-sm font-semibold text-muted">Premier rappel</label>
                  <select className="w-full p-3 rounded-xl border border-gray-200 bg-background outline-none focus:ring-1 focus:ring-primary">
                      <option>48 heures avant</option>
                      <option>24 heures avant</option>
                      <option>1 semaine avant</option>
                  </select>
              </div>
               <div className="space-y-2">
                  <label className="text-sm font-semibold text-muted">Dernier rappel</label>
                  <select className="w-full p-3 rounded-xl border border-gray-200 bg-background outline-none focus:ring-1 focus:ring-primary">
                      <option>2 heures avant</option>
                      <option>4 heures avant</option>
                      <option>Le matin même</option>
                  </select>
              </div>
          </div>
      </div>

      <div className="flex justify-end gap-4 pt-4">
          <button className="px-6 py-3 font-bold text-muted hover:text-text bg-white border border-gray-200 rounded-xl">Annuler</button>
          <button onClick={handleSave} className="px-8 py-3 font-bold text-white bg-primary rounded-xl shadow-lg shadow-primary/20 hover:bg-primary-dark">Enregistrer</button>
      </div>
    </div>
  );
}