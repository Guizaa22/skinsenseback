
# Document de fonctionnalités

## Application “Centre de beauté & soins”

**Version à valider par la cliente / propriétaire**
**Date :** Aujourd'hui

---

## Objectif du projet

Mettre en place un système simple et fiable pour :

* Permettre aux clientes de réserver un rendez-vous en ligne avec un employé précis (selon ses horaires).
* Permettre à la propriétaire (**Admin**) de gérer clientes, employés, horaires, services et RDV.
* Permettre aux employés de gérer leurs RDV et de renseigner le suivi (notes professionnelles).
* Réduire les oublis via rappels SMS + email.

## Applications incluses

* **Application Web Cliente** : accessible sur téléphone/PC.
* **Application Staff/Admin** : optimisée “ordinateur”, utilisable comme “desktop app” si besoin.
* **Système de notifications** : Email + SMS.

## Rôles

* **Cliente** : Réserve, remplit son dossier, consulte son historique.
* **Employé** : Voit ses RDV, consulte les dossiers clients, ajoute des notes professionnelles.
* **Admin** (Propriétaire) : Tout gérer (employés, services, horaires, RDV, accès).

---

# PHASE 1 — MVP

*Ce qui doit être prêt pour une première mise en service.*

### A) Gestion des services (Admin)

L’Admin peut :

* Créer / modifier / désactiver un service (prestation).
* Définir durée, prix (même si paiement cash), description.
* Définir spécialité requise (ou quels employés peuvent le faire).

**But :** Même si la liste des services change, l’Admin garde la main.

### B) Gestion des employés (Admin)

L’Admin peut :

* Créer les comptes employés.
* Définir leurs spécialités.
* Définir leurs jours & horaires de travail.
* Ajouter des congés / absences.

### C) Réservation en ligne (Cliente)

La Cliente peut :

* Voir la liste des employés et leurs horaires.
* Choisir un employé précis.
* Choisir un service (filtré selon les spécialités de l’employé).
* Voir les créneaux disponibles et réserver.

> **Règles MVP**
> Les créneaux respectent les horaires de travail, les congés, et évitent les chevauchements de RDV.

### D) Gestion des RDV (Employé + Admin)

* Vue agenda (jour/semaine) + liste des RDV.
* Création / modification / annulation de RDV (selon droits).
* Statuts (ex : Confirmé / Annulé / Terminé).

### E) Dossier Cliente (formulaire d’accueil) — “Fiche Client”

La Cliente remplit / met à jour une fiche structurée incluant :

* Identité & contact + comment elle a connu le centre.
* Motif de consultation + objectif + type de soin (Nettoyage / HydraFacial / Microneedling / autre).
* Antécédents médicaux + traitements/médicaments.
* Allergies/réactions + habitudes + routine skincare.
* Historique esthétique (peeling/laser/microneedling/injections + date).
* Autorisation photos (suivi / communication).

### F) Notes professionnelles (Employé/Admin)

Après un soin, le staff peut renseigner la partie *“réservée au professionnel”* :

* Diagnostic, phototype, soin réalisé, produits/paramètres, réactions, recommandations, prochain RDV.

> **Recommandation de droits**
> * **Cliente :** modifie sa partie “déclarative” (infos, historique, allergies…).
> * **Employé/Admin :** modifie uniquement les notes professionnelles.
> * *Option :* la cliente peut voir les notes pro, mais ne les modifie pas.
> 
> 

### G) Historique des RDV (Cliente + Staff)

Historique par cliente : date, prestation, prix, produits utilisés, notes (selon droits).
*(Les “produits utilisés” peuvent être en champ texte au début, puis transformés en liste structurée plus tard.)*

### H) Notifications (Email + SMS)

* Email/SMS de confirmation de réservation.
* Rappels automatiques (ex : 24h avant, 2h avant — paramétrable).

**Important :** prévoir une gestion simple du consentement et désinscription pour les SMS (bonne pratique de conformité).

### I) Sécurité & confidentialité (minimum MVP)

Comme le dossier contient des informations médicales (allergies, antécédents…), on traite ces données comme sensibles et on applique :

* Accès par rôle (cliente / employé / admin).
* Historique des modifications (audit).
* Stockage sécurisé + connexions HTTPS.

> **Référence Tunisie**
> L’INPDP liste le cadre légal et notamment la **Loi organique n°2004-63** sur la protection des données personnelles. *(Note : ce document n’est pas un avis juridique.)*

---

# PHASE 2 — Croissance (Fonctions très demandées)

*Après validation et usage réel du MVP.*

### A) Points de fidélité + VIP

* Points gagnés (par montant ou par service).
* Récompenses (réduction / service offert / bonus).
* Statut VIP (niveaux + avantages).

### B) Forfaits / cures (ex : 6 séances)

* Suivi “séance 1/6, 2/6 …”.
* Résultats et remarques par séance.

### C) Liste d’attente + automatisation

* Si un créneau est complet : “s’inscrire en liste d’attente”.
* Si annulation : proposition automatique aux personnes en attente (SMS/email).

### D) Google Calendar (option)

Synchronisation des RDV vers Google Calendar (au moins côté Admin/Employés).

### E) WhatsApp (option)

* Bouton “Contacter sur WhatsApp”.
* Messages pré-remplis (confirmation, rappel, infos).

---

# PHASE 3 — Avancé (Automatisation & expérience premium)

### A) Chatbot / FAQ (IA)

* Réponses sur les prestations, prix, durées, après-soin, contre-indications.
* Basé sur le contenu validé du centre (pas “au hasard” sur Internet).

### B) Paiement en ligne (acompte / avance)

* Ajout d’un acompte optionnel.
* Statut de paiement (cash / carte / en ligne).

### C) Catalogue produits & stock (si besoin)

Produits utilisés en soins + suivi de stock.