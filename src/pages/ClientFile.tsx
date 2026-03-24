import React, { useState, useEffect } from 'react';
import { Save, User, FileHeart, Sparkles, AlertCircle, Loader2 } from 'lucide-react';
import { api } from '@/src/services/api.ts';
import { useAuth } from '@/src/context/AuthContext.tsx';
import { ClientFile as ClientFileType } from '../../types.ts';

export default function ClientFile() {
  const { user } = useAuth();
  const [profile, setProfile] = useState<ClientFileType | null>(null);
  const [loading, setLoading] = useState(true);
  const [hasChanges, setHasChanges] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [saveError, setSaveError] = useState('');
  const [clientPhotos, setClientPhotos] = useState<any[]>([]);
  const [loadingPhotos, setLoadingPhotos] = useState(false);

  useEffect(() => {
    api.clientFile.get()
        .then(res => {
          const rawFile = res.data;
          const intake = rawFile.intake ?? {};
          setProfile({
            ...rawFile,
            photoConsentForFollowUp: rawFile.photoConsentForFollowup ?? rawFile.photoConsentForFollowUp,
            intake: {
              ...intake,
              howFoundUs: typeof intake.howFoundUs === 'string' && intake.howFoundUs
                ? intake.howFoundUs.split(',').map((s: string) => s.trim())
                : Array.isArray(intake.howFoundUs) ? intake.howFoundUs : [],
              consultationTypes: typeof intake.consultationTypes === 'string' && intake.consultationTypes
                ? intake.consultationTypes.split(',').map((s: string) => s.trim())
                : Array.isArray(intake.consultationTypes) ? intake.consultationTypes : [],
            },
          });
        })
        .catch(err => console.error(err))
        .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    setLoadingPhotos(true);
    api.photos.getClientPhotos()
      .then((res: any) => {
        const photos = Array.isArray(res?.data) ? res.data : [];
        setClientPhotos(photos);
      })
      .catch(() => setClientPhotos([]))
      .finally(() => setLoadingPhotos(false));
  }, []);

  const handleIntakeChange = (field: string, value: string) => {
    if (!profile) return;
    setProfile({
        ...profile,
        intake: { ...profile.intake, [field]: value }
    });
    setHasChanges(true);
  };

  const handleMedicalChange = (field: string, value: string) => {
    if (!profile) return;
    setProfile({
        ...profile,
        medicalHistory: { ...profile.medicalHistory, [field]: value }
    });
    setHasChanges(true);
  };

  const handleAestheticChange = (field: string, value: string) => {
    if (!profile) return;
    setProfile({
        ...profile,
        aestheticProcedureHistory: { ...profile.aestheticProcedureHistory, [field]: value }
    });
    setHasChanges(true);
  };

  const handleMedicalBoolChange = (field: string, value: boolean) => {
    if (!profile) return;
    setProfile({
      ...profile,
      medicalHistory: { ...profile.medicalHistory, [field]: value }
    });
    setHasChanges(true);
  };

  const handleAestheticBoolChange = (field: string, value: boolean) => {
    if (!profile) return;
    setProfile({
      ...profile,
      aestheticProcedureHistory: { ...profile.aestheticProcedureHistory, [field]: value }
    });
    setHasChanges(true);
  };

  const toggleIntakeArray = (field: 'howFoundUs' | 'consultationTypes', option: string) => {
    if (!profile) return;
    const intake = profile.intake as Record<string, unknown>;
    const current: string[] = Array.isArray(intake[field]) ? (intake[field] as string[]) : [];
    const next = current.includes(option) ? current.filter(x => x !== option) : [...current, option];
    setProfile({
      ...profile,
      intake: { ...profile.intake, [field]: next }
    });
    setHasChanges(true);
  };

  const handleSave = async () => {
      if (!profile) return;
      setSaveError('');
      setIsSaving(true);
      try {
        const intake = profile.intake as any;
        const mh = profile.medicalHistory as any;
        const aph = profile.aestheticProcedureHistory as any;
        const payload = {
          intake: {
            howDidYouHearAboutUs: profile.intake?.howDidYouHearAboutUs ?? '',
            consultationReason: profile.intake?.consultationReason ?? '',
            objective: profile.intake?.objective ?? '',
            careType: profile.intake?.careType ?? '',
            skincareRoutine: profile.intake?.skincareRoutine ?? '',
            habits: profile.intake?.habits ?? '',
            dateOfBirth: intake?.dateOfBirth ?? '',
            address: intake?.address ?? '',
            profession: intake?.profession ?? '',
            howFoundUs: Array.isArray(intake?.howFoundUs)
              ? intake.howFoundUs.join(',')
              : (intake?.howFoundUs ?? ''),
            consultationTypes: Array.isArray(intake?.consultationTypes)
              ? intake.consultationTypes.join(',')
              : (intake?.consultationTypes ?? ''),
          },
          medicalHistory: {
            medicalBackground: profile.medicalHistory?.medicalBackground ?? '',
            currentTreatments: profile.medicalHistory?.currentTreatments ?? '',
            allergiesAndReactions: profile.medicalHistory?.allergiesAndReactions ?? '',
            grossesse: Boolean((profile as any).grossesse ?? mh?.grossesse ?? false),
            diabete: Boolean((profile as any).diabete ?? mh?.diabete ?? false),
            maladieAutoImmune: Boolean((profile as any).maladieAutoImmune ?? mh?.maladieAutoImmune ?? false),
            troublesHormonaux: Boolean((profile as any).troublesHormonaux ?? mh?.troublesHormonaux ?? false),
            problemsCicatrisation: Boolean((profile as any).problemsCicatrisation ?? mh?.problemsCicatrisation ?? false),
            herpes: Boolean((profile as any).herpes ?? mh?.herpes ?? false),
            epilepsie: Boolean((profile as any).epilepsie ?? mh?.epilepsie ?? false),
            cancer: Boolean((profile as any).cancer ?? mh?.cancer ?? false),
            maladiesDermatologiques: Boolean((profile as any).maladiesDermatologiques ?? mh?.maladiesDermatologiques ?? false),
            interventionChirurgicale: Boolean((profile as any).interventionChirurgicale ?? mh?.interventionChirurgicale ?? false),
            isotretinoine: Boolean((profile as any).isotretinoine ?? mh?.isotretinoine ?? false),
            corticoides: Boolean((profile as any).corticoides ?? mh?.corticoides ?? false),
            anticoagulants: Boolean((profile as any).anticoagulants ?? mh?.anticoagulants ?? false),
            hormones: Boolean((profile as any).hormones ?? mh?.hormones ?? false),
            antibiotiques: Boolean((profile as any).antibiotiques ?? mh?.antibiotiques ?? false),
            tabac: Boolean((profile as any).tabac ?? mh?.tabac ?? false),
            expositionSolaire: Boolean((profile as any).expositionSolaire ?? mh?.expositionSolaire ?? false),
            cabineUV: Boolean((profile as any).cabineUV ?? mh?.cabineUV ?? false),
            sportIntensif: Boolean((profile as any).sportIntensif ?? mh?.sportIntensif ?? false),
            stressImportant: Boolean((profile as any).stressImportant ?? mh?.stressImportant ?? false),
          },
          aestheticProcedureHistory: {
            procedures: profile.aestheticProcedureHistory?.procedures ?? '',
            peeling: Boolean((profile as any).peeling ?? aph?.peeling ?? false),
            laser: Boolean((profile as any).laser ?? aph?.laser ?? false),
            microneedlingHistory: Boolean((profile as any).microneedlingHistory ?? aph?.microneedlingHistory ?? false),
            injections: Boolean((profile as any).injections ?? aph?.injections ?? false),
            dateDernierSoin: aph?.dateDernierSoin ?? '',
          },
          photoConsentForFollowup: Boolean(profile.photoConsentForFollowUp ?? (profile as any).photoConsentForFollowup ?? false),
          photoConsentForMarketing: Boolean(profile.photoConsentForMarketing ?? false),
        };
        const res = await api.clientFile.update(payload);
        const updated = res.data;
        setProfile({
          ...updated,
          photoConsentForFollowUp: updated.photoConsentForFollowup ?? updated.photoConsentForFollowUp,
        });
        setHasChanges(false);
        setSaved(true);
        setTimeout(() => setSaved(false), 3000);
      } catch (err: any) {
          setSaveError(err?.message || 'Erreur lors de la sauvegarde');
          setTimeout(() => setSaveError(''), 4000);
      } finally {
          setIsSaving(false);
      }
  };

  if (loading || !profile) {
      return <div className="flex justify-center py-20"><Loader2 className="w-8 h-8 animate-spin text-primary" /></div>;
  }

  return (
    <div className="max-w-3xl mx-auto pb-24 md:pb-8 space-y-8 animate-in slide-in-from-bottom-4 duration-500">
      
      {/* Header with sticky save bar */}
      <div className="flex flex-col sm:flex-row justify-between sm:items-center gap-3 mb-6 sticky top-0 bg-background/95 backdrop-blur-sm z-20 py-4 border-b border-transparent md:border-none">
        <div>
            <h1 className="text-3xl font-black text-text">Mon Dossier Médical</h1>
            <p className="text-muted mt-1">Informations confidentielles pour personnaliser vos soins.</p>
        </div>
        
        {(saved || saveError) && (
            <>
              {saved && (
                <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-xl text-sm font-medium mb-4">
                  ✓ Dossier sauvegardé avec succès
                </div>
              )}
              {saveError && (
                <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-xl text-sm font-medium mb-4">
                  {saveError}
                </div>
              )}
            </>
        )}
        {hasChanges && (
            <div className="flex gap-3 animate-in fade-in slide-in-from-right-4">
                <button 
                    onClick={() => { window.location.reload(); }}
                    className="px-4 py-2 text-sm font-bold text-muted hover:text-text bg-white border border-gray-200 rounded-lg"
                >
                    Annuler
                </button>
                <button onClick={handleSave} disabled={isSaving} className="px-4 py-2 text-sm font-bold text-white bg-primary rounded-lg shadow-md hover:bg-primary-dark flex items-center gap-2">
                    {isSaving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
                    Enregistrer
                </button>
            </div>
        )}
      </div>

      {/* GDPR Info */}
      <div className="bg-blue-50 border border-blue-100 rounded-2xl p-4 flex gap-3">
        <AlertCircle className="w-5 h-5 text-blue-600 shrink-0 mt-0.5" />
        <p className="text-sm text-blue-800">Seuls vous et le personnel soignant avez accès à ces données (cryptées et sécurisées).</p>
      </div>

      {/* Identity & Intake */}
      <section className="bg-white rounded-2xl p-6 md:p-8 shadow-sm border border-gray-100">
        <h2 className="text-xl font-bold mb-6 flex items-center gap-2" style={{ fontFamily: "'Cormorant Garamond', serif", color: '#534741' }}>
            <User className="w-5 h-5" style={{ color: '#A17969' }} />
            Identité & Informations
        </h2>
        
        {/* User Identity - Read Only */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8 pb-8 border-b border-gray-100">
             <div className="space-y-2">
                <label className="text-sm font-semibold text-text">Nom complet</label>
                <div className="w-full px-4 py-3 rounded-xl border border-gray-200 bg-gray-50 text-gray-500">
                    {user?.fullName}
                </div>
            </div>
            <div className="space-y-2">
                <label className="text-sm font-semibold text-text">Email</label>
                <div className="w-full px-4 py-3 rounded-xl border border-gray-200 bg-gray-50 text-gray-500">
                    {user?.email}
                </div>
            </div>
            <div className="space-y-2">
                <label className="text-sm font-semibold text-text">Téléphone</label>
                <div className="w-full px-4 py-3 rounded-xl border border-gray-200 bg-gray-50 text-gray-500">
                    {user?.phone}
                </div>
            </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-2">
                <label className="text-sm font-semibold text-text">Comment nous avez-vous connus ?</label>
                <input 
                    type="text" 
                    value={profile.intake.howDidYouHearAboutUs}
                    onChange={(e) => handleIntakeChange('howDidYouHearAboutUs', e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all"
                />
            </div>
            <div className="space-y-2">
                <label className="text-sm font-semibold text-text">Motif de consultation</label>
                <input 
                    type="text" 
                    value={profile.intake.consultationReason}
                    onChange={(e) => handleIntakeChange('consultationReason', e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all"
                />
            </div>
             <div className="space-y-2">
                <label className="text-sm font-semibold text-text">Objectif souhaité</label>
                <input 
                    type="text" 
                    value={profile.intake.objective}
                    onChange={(e) => handleIntakeChange('objective', e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all"
                />
            </div>
            <div className="space-y-2">
                <label className="text-sm font-semibold text-text">Type de soin recherché</label>
                <select 
                    value={profile.intake.careType}
                    onChange={(e) => handleIntakeChange('careType', e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all bg-white"
                >
                    <option value="">Sélectionner...</option>
                    <option value="Visage">Visage</option>
                    <option value="Corps">Corps</option>
                    <option value="Epilation">Epilation</option>
                    <option value="Autre">Autre</option>
                </select>
            </div>
            <div className="space-y-2">
                <label className="text-sm font-semibold" style={{ color: '#736357' }}>Date de naissance</label>
                <input
                    type="text"
                    value={(profile.intake as any).dateOfBirth ?? ''}
                    onChange={(e) => handleIntakeChange('dateOfBirth', e.target.value)}
                    placeholder="JJ/MM/AAAA"
                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-[#A17969] focus:ring-1 focus:ring-[#A17969] outline-none transition-all"
                    style={{ color: '#534741' }}
                />
            </div>
            <div className="space-y-2">
                <label className="text-sm font-semibold" style={{ color: '#736357' }}>Adresse / Région</label>
                <input
                    type="text"
                    value={(profile.intake as any).address ?? ''}
                    onChange={(e) => handleIntakeChange('address', e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-[#A17969] focus:ring-1 focus:ring-[#A17969] outline-none transition-all"
                    style={{ color: '#534741' }}
                />
            </div>
            <div className="space-y-2">
                <label className="text-sm font-semibold" style={{ color: '#736357' }}>Profession</label>
                <input
                    type="text"
                    value={(profile.intake as any).profession ?? ''}
                    onChange={(e) => handleIntakeChange('profession', e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-[#A17969] focus:ring-1 focus:ring-[#A17969] outline-none transition-all"
                    style={{ color: '#534741' }}
                />
            </div>
        </div>

        <h3 className="text-lg font-bold mt-8 mb-3" style={{ fontFamily: "'Cormorant Garamond', serif", color: '#534741' }}>Comment avez-vous connu SkinSense ?</h3>
        <div className="flex flex-wrap gap-4 mb-6">
          {['Instagram', 'Bouche à oreille', 'Réseaux sociaux', 'Google', 'ChatGPT', 'Autre'].map(opt => {
            const arr = (profile.intake as any).howFoundUs ?? [];
            const checked = Array.isArray(arr) && arr.includes(opt);
            return (
              <label key={opt} className="flex items-center gap-2 cursor-pointer" style={{ color: '#736357' }}>
                <input type="checkbox" checked={checked} onChange={() => toggleIntakeArray('howFoundUs', opt)} className="rounded" style={{ accentColor: '#A17969' }} />
                <span className="text-sm">{opt}</span>
              </label>
            );
          })}
        </div>

        <h3 className="text-lg font-bold mb-3" style={{ fontFamily: "'Cormorant Garamond', serif", color: '#534741' }}>Type de soin souhaité</h3>
        <div className="flex flex-wrap gap-4">
          {['Nettoyage de peau', 'HydraFacial', 'Microneedling', 'Autre'].map(opt => {
            const arr = (profile.intake as any).consultationTypes ?? [];
            const checked = Array.isArray(arr) && arr.includes(opt);
            return (
              <label key={opt} className="flex items-center gap-2 cursor-pointer" style={{ color: '#736357' }}>
                <input type="checkbox" checked={checked} onChange={() => toggleIntakeArray('consultationTypes', opt)} className="rounded" style={{ accentColor: '#A17969' }} />
                <span className="text-sm">{opt}</span>
              </label>
            );
          })}
        </div>
      </section>

      {/* Medical History */}
      <section className="bg-white rounded-2xl p-6 md:p-8 shadow-sm border border-gray-100">
        <h2 className="text-xl font-bold mb-6 flex items-center gap-2" style={{ fontFamily: "'Cormorant Garamond', serif", color: '#534741' }}>
            <FileHeart className="w-5 h-5" style={{ color: '#A17969' }} />
            Antécédents Médicaux
        </h2>

        <div className="space-y-6">
            <div className="space-y-2">
                <label className="text-sm font-semibold text-text">Antécédents médicaux</label>
                 <textarea 
                    value={profile.medicalHistory.medicalBackground}
                    onChange={(e) => handleMedicalChange('medicalBackground', e.target.value)}
                    placeholder="Maladies chroniques, opérations..."
                    rows={2}
                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all resize-none"
                />
            </div>
            <div className="space-y-2">
                <label className="text-sm font-semibold text-text">Allergies et réactions</label>
                <textarea 
                    value={profile.medicalHistory.allergiesAndReactions}
                    onChange={(e) => handleMedicalChange('allergiesAndReactions', e.target.value)}
                    placeholder="Ex: Nickel, parfums, conservateurs..."
                    rows={2}
                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all resize-none"
                />
            </div>
            <div className="space-y-2">
                <label className="text-sm font-semibold text-text">Traitements en cours</label>
                <textarea 
                    value={profile.medicalHistory.currentTreatments}
                    onChange={(e) => handleMedicalChange('currentTreatments', e.target.value)}
                    placeholder="Médicaments..."
                    rows={2}
                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all resize-none"
                />
            </div>

            <h3 className="text-lg font-bold mt-6 mb-3" style={{ fontFamily: "'Cormorant Garamond', serif", color: '#534741' }}>Antécédents médicaux</h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-6">
              {[
                { key: 'grossesse', label: 'Grossesse / allaitement' },
                { key: 'diabete', label: 'Diabète' },
                { key: 'maladieAutoImmune', label: 'Maladie auto-immune' },
                { key: 'troublesHormonaux', label: 'Troubles hormonaux' },
                { key: 'problemsCicatrisation', label: 'Problèmes de cicatrisation' },
                { key: 'herpes', label: 'Herpès (HSV)' },
                { key: 'epilepsie', label: 'Épilepsie' },
                { key: 'cancer', label: 'Cancer' },
                { key: 'maladiesDermatologiques', label: 'Maladie dermatologique' },
                { key: 'interventionChirurgicale', label: 'Intervention chirurgicale récente' },
              ].map(({ key, label }) => (
                <label key={key} className="flex items-center gap-2 cursor-pointer" style={{ color: '#736357' }}>
                  <input type="checkbox" checked={!!(profile.medicalHistory as any)[key]} onChange={(e) => handleMedicalBoolChange(key, e.target.checked)} className="rounded" style={{ accentColor: '#A17969' }} />
                  <span className="text-sm">{label}</span>
                </label>
              ))}
            </div>

            <h3 className="text-lg font-bold mb-3" style={{ fontFamily: "'Cormorant Garamond', serif", color: '#534741' }}>Traitements & médicaments</h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-6">
              {[
                { key: 'isotretinoine', label: 'Isotrétinoïne (Roaccutane)' },
                { key: 'corticoides', label: 'Corticoïdes' },
                { key: 'anticoagulants', label: 'Anticoagulants' },
                { key: 'hormones', label: 'Hormones' },
                { key: 'antibiotiques', label: 'Antibiotiques récents' },
              ].map(({ key, label }) => (
                <label key={key} className="flex items-center gap-2 cursor-pointer" style={{ color: '#736357' }}>
                  <input type="checkbox" checked={!!(profile.medicalHistory as any)[key]} onChange={(e) => handleMedicalBoolChange(key, e.target.checked)} className="rounded" style={{ accentColor: '#A17969' }} />
                  <span className="text-sm">{label}</span>
                </label>
              ))}
            </div>

            <h3 className="text-lg font-bold mb-3" style={{ fontFamily: "'Cormorant Garamond', serif", color: '#534741' }}>Habitudes & mode de vie</h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-6">
              {[
                { key: 'tabac', label: 'Tabac' },
                { key: 'expositionSolaire', label: 'Exposition solaire fréquente' },
                { key: 'cabineUV', label: 'Cabine UV' },
                { key: 'sportIntensif', label: 'Sport intensif' },
                { key: 'stressImportant', label: 'Stress important' },
              ].map(({ key, label }) => (
                <label key={key} className="flex items-center gap-2 cursor-pointer" style={{ color: '#736357' }}>
                  <input type="checkbox" checked={!!(profile.medicalHistory as any)[key]} onChange={(e) => handleMedicalBoolChange(key, e.target.checked)} className="rounded" style={{ accentColor: '#A17969' }} />
                  <span className="text-sm">{label}</span>
                </label>
              ))}
            </div>

             <div className="space-y-2">
                <label className="text-sm font-semibold text-text">Historique esthétique</label>
                <textarea 
                    value={profile.aestheticProcedureHistory.procedures}
                    onChange={(e) => handleAestheticChange('procedures', e.target.value)}
                    placeholder="Botox, Peelings, Laser..."
                    rows={2}
                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all resize-none"
                />
            </div>
        </div>
      </section>

      {/* Routine */}
      <section className="bg-white rounded-2xl p-6 md:p-8 shadow-sm border border-gray-100">
        <h2 className="text-xl font-bold mb-6 flex items-center gap-2" style={{ fontFamily: "'Cormorant Garamond', serif", color: '#534741' }}>
            <Sparkles className="w-5 h-5" style={{ color: '#A17969' }} />
            Routine & Habitudes
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
            <div className="space-y-2">
                <label className="text-sm font-semibold text-text">Routine Skincare actuelle</label>
                <textarea 
                    rows={3}
                    value={profile.intake.skincareRoutine}
                    onChange={(e) => handleIntakeChange('skincareRoutine', e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all resize-none"
                />
            </div>
            <div className="space-y-2">
                <label className="text-sm font-semibold text-text">Habitudes de vie (Soleil, Tabac...)</label>
                <textarea 
                    rows={3}
                    value={profile.intake.habits}
                    onChange={(e) => handleIntakeChange('habits', e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all resize-none"
                />
            </div>
        </div>
      </section>

      {/* Aesthetic History */}
      <section className="bg-white rounded-2xl p-6 md:p-8 shadow-sm border border-gray-100">
        <h2 className="text-xl font-bold mb-6 flex items-center gap-2" style={{ fontFamily: "'Cormorant Garamond', serif", color: '#534741' }}>
          Historique esthétique
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-6">
          {[
            { key: 'peeling', label: 'Peeling' },
            { key: 'laser', label: 'Laser / IPL' },
            { key: 'microneedlingHistory', label: 'Microneedling' },
            { key: 'injections', label: 'Injections' },
          ].map(({ key, label }) => (
            <label key={key} className="flex items-center gap-2 cursor-pointer" style={{ color: '#736357' }}>
              <input type="checkbox" checked={!!(profile.aestheticProcedureHistory as any)[key]} onChange={(e) => handleAestheticBoolChange(key, e.target.checked)} className="rounded" style={{ accentColor: '#A17969' }} />
              <span className="text-sm">{label}</span>
            </label>
          ))}
        </div>
        <div className="space-y-2">
          <label className="text-sm font-semibold" style={{ color: '#736357' }}>Date du dernier soin</label>
          <input
            type="text"
            value={(profile.aestheticProcedureHistory as any).dateDernierSoin ?? ''}
            onChange={(e) => handleAestheticChange('dateDernierSoin', e.target.value)}
            placeholder="JJ/MM/AAAA"
            className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-[#A17969] focus:ring-1 focus:ring-[#A17969] outline-none transition-all"
            style={{ color: '#534741' }}
          />
        </div>
      </section>

      {/* Consents */}
      <section className="bg-white rounded-2xl p-6 md:p-8 shadow-sm border border-gray-100 space-y-4">
        <div className="flex items-center justify-between">
            <div>
                <h3 className="text-sm font-bold text-text">Autorisation photos (Suivi médical)</h3>
                <p className="text-xs text-muted">Permettre l'utilisation de vos photos pour le suivi interne (obligatoire pour certains soins).</p>
            </div>
            <button 
                onClick={() => setProfile({...profile, photoConsentForFollowUp: !profile.photoConsentForFollowUp})}
                className={`w-12 h-6 rounded-full p-1 transition-colors ${profile.photoConsentForFollowUp ? 'bg-primary' : 'bg-gray-200'}`}
            >
                <div className={`w-4 h-4 bg-white rounded-full transition-transform ${profile.photoConsentForFollowUp ? 'translate-x-6' : 'translate-x-0'}`} />
            </button>
        </div>
        <div className="h-px bg-gray-100" />
        <div className="flex items-center justify-between">
            <div>
                <h3 className="text-sm font-bold text-text">Autorisation photos (Marketing)</h3>
                <p className="text-xs text-muted">Autoriser l'utilisation anonymisée sur nos réseaux sociaux.</p>
            </div>
            <button 
                onClick={() => setProfile({...profile, photoConsentForMarketing: !profile.photoConsentForMarketing})}
                className={`w-12 h-6 rounded-full p-1 transition-colors ${profile.photoConsentForMarketing ? 'bg-primary' : 'bg-gray-200'}`}
            >
                <div className={`w-4 h-4 bg-white rounded-full transition-transform ${profile.photoConsentForMarketing ? 'translate-x-6' : 'translate-x-0'}`} />
            </button>
        </div>
      </section>

      {/* Before / After Photos */}
      {(loadingPhotos || clientPhotos.length > 0) && (
        <section className="bg-white rounded-2xl p-6 md:p-8 shadow-sm border border-gray-100">
          <h2 className="text-xl font-bold mb-6 flex items-center gap-2" style={{ fontFamily: "'Cormorant Garamond', serif", color: '#534741' }}>
            📷 Photos Avant / Après
          </h2>
          {loadingPhotos ? (
            <div className="flex justify-center py-8">
              <Loader2 className="w-6 h-6 animate-spin" style={{ color: '#A17969' }} />
            </div>
          ) : (
            <div className="space-y-6">
              {/* Before photos */}
              {clientPhotos.filter(p => p.label === 'before').length > 0 && (
                <div>
                  <h3 className="text-sm font-bold uppercase tracking-wider mb-3" style={{ color: '#A17969' }}>Avant</h3>
                  <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
                    {clientPhotos.filter(p => p.label === 'before').map((photo: any) => (
                      <div key={photo.id} className="rounded-xl overflow-hidden bg-gray-100 aspect-square">
                        <img
                          src={api.photos.getImageUrl(photo.url)}
                          alt="avant"
                          className="w-full h-full object-cover"
                        />
                      </div>
                    ))}
                  </div>
                </div>
              )}
              {/* After photos */}
              {clientPhotos.filter(p => p.label === 'after').length > 0 && (
                <div>
                  <h3 className="text-sm font-bold uppercase tracking-wider mb-3" style={{ color: '#A17969' }}>Après</h3>
                  <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
                    {clientPhotos.filter(p => p.label === 'after').map((photo: any) => (
                      <div key={photo.id} className="rounded-xl overflow-hidden bg-gray-100 aspect-square">
                        <img
                          src={api.photos.getImageUrl(photo.url)}
                          alt="après"
                          className="w-full h-full object-cover"
                        />
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </section>
      )}

    </div>
  );
}