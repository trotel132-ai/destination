<!DOCTYPE html>
html lang="de"
head>
<meta charset="UTF-8">
<title>DreamTrip – Individuelle Urlaubsdesigner</title>
<meta name="viewport" content="width=device-width, initial-scale=1">

<!-- ================= FIREBASE SDK ================= -->
<script src="https://www.gstatic.com/firebasejs/10.7.1/firebase-app-compat.js"></script>
<script src="https://www.gstatic.com/firebasejs/10.7.1/firebase-auth-compat.js"></script>
<script src="https://www.gstatic.com/firebasejs/10.7.1/firebase-firestore-compat.js"></script>

<!-- ================= DESIGN ================= -->
<style>
body{margin:0;font-family:Arial;background:#fff;color:#111}
header{position:fixed;top:0;width:100%;padding:15px 30px;
  display:flex;justify-content:space-between;
  background:rgba(255,255,255,.9);
  box-shadow:0 5px 20px rgba(0,0,0,.1);z-index:10}
.hero{height:100vh;background:url('https://images.unsplash.com/photo-1500530855697-b586d89ba3ee') center/cover;
  display:flex;align-items:center;justify-content:center;color:#fff;text-align:center}
.hero div{background:rgba(0,0,0,.55);padding:50px;border-radius:30px}
section{padding:120px 20px;max-width:1100px;margin:auto}
.btn{padding:12px 30px;border-radius:30px;border:none;cursor:pointer;font-size:1rem}
input,textarea,select{width:100%;padding:12px;margin:6px 0;border-radius:10px;border:1px solid #ccc}
.hidden{display:none}
.card{padding:20px;border-radius:20px;box-shadow:0 10px 30px rgba(0,0,0,.15);margin:10px 0}
.step{opacity:.3;padding:14px;border-radius:12px;background:#eee;margin:8px 0;transition:.4s}
.step.active{opacity:1;background:#000;color:#fff}
</style>
</head>

<body>

<!-- ================= HEADER ================= -->
<header>
  <b>DreamTrip</b>
  <div>
    <button onclick="showLogin()">Login</button>
    <button onclick="logout()">Logout</button>
  </div>
</header>

<!-- ================= HERO ================= -->
<div class="hero">
  <div>
    <h1>Dein Urlaub. Dein Design.</h1>
    <p>Individuell geplant · KI-gestützt · Persönlich betreut</p>
    <button class="btn" onclick="scrollToBooking()">Jetzt starten</button>
  </div>
</div>

<!-- ================= BUCHUNG ================= -->
<section id="booking">
<h2>Reise anfragen</h2>

<form id="bookingForm">
<input name="name" placeholder="Name" required>
<input name="email" type="email" placeholder="E-Mail" required>
<input name="phone" placeholder="Telefon" required>
<input name="budget" placeholder="Budget (€)" required>

<select name="type" onchange="suggestTrips(this.value)" required>
  <option value="">Reisetyp wählen</option>
  <option value="strand">Strand</option>
  <option value="abenteuer">Abenteuer</option>
  <option value="luxus">Luxus</option>
</select>

<div id="suggestions"></div>

<textarea name="wishes" placeholder="Besondere Wünsche"></textarea>

<button class="btn">Design anfragen</button>
</form>

<!-- ====== Prozess nach Buchung ====== -->
<div id="process" class="hidden">
  <div class="step">🧠 Analyse deiner Wünsche</div>
  <div class="step">✈️ Individuelles Reisedesign</div>
  <div class="step">💳 Zahlungsprüfung</div>
  <div class="step">🌴 Dein Urlaub ist startklar</div>
</div>
</section>

<!-- ================= ADMIN ================= -->
<section id="admin" class="hidden">
<h2>Admin Dashboard</h2>
<div id="adminBookings"></div>
</section>

<!-- ================= LOGIN ================= -->
<div id="loginModal" class="hidden">
<section>
<h2>Login / Registrierung</h2>
<input id="loginEmail" placeholder="E-Mail">
<input id="loginPassword" type="password" placeholder="Passwort">
<button class="btn" onclick="login()">Login</button>
<button class="btn" onclick="register()">Registrieren</button>
</section>
</div>

<script>
/* =================================================
   FIREBASE KONFIGURATION
   ================================================= */
const firebaseConfig = {
  apiKey: "DEIN_API_KEY",
  authDomain: "DEIN_PROJEKT.firebaseapp.com",
  projectId: "DEIN_PROJEKT",
};
firebase.initializeApp(firebaseConfig);

const auth = firebase.auth();
const db = firebase.firestore();

/* =================================================
   GLOBALS
   ================================================= */
let currentUser = null;
let isAdmin = false;

/* =================================================
   KI-REISEVORSCHLÄGE
   ================================================= */
const aiTrips = {
  strand: ["Bali", "Malediven", "Seychellen"],
  abenteuer: ["Island", "Neuseeland", "Costa Rica"],
  luxus: ["Dubai", "Bora Bora", "Amalfiküste"]
};

function suggestTrips(type){
  document.getElementById("suggestions").innerHTML =
    "🤖 KI-Vorschläge: <b>" + aiTrips[type].join(", ") + "</b>";
}

/* =================================================
   LOGIN / AUTH
   ================================================= */
function showLogin(){
  document.getElementById("loginModal").classList.remove("hidden");
}

function register(){
  auth.createUserWithEmailAndPassword(
    loginEmail.value, loginPassword.value
  ).then(()=>alert("Registriert!"));
}

function login(){
  auth.signInWithEmailAndPassword(
    loginEmail.value, loginPassword.value
  );
}

function logout(){
  auth.signOut();
  location.reload();
}

/* =================================================
   AUTH STATE
   ================================================= */
auth.onAuthStateChanged(user=>{
  if(user){
    currentUser = user;

    // Admin prüfen
    db.collection("admins").doc(user.uid).get().then(doc=>{
      isAdmin = doc.exists;
      if(isAdmin){
        document.getElementById("admin").classList.remove("hidden");
      }
    });

    document.getElementById("loginModal").classList.add("hidden");
  }
});

/* =================================================
   BUCHUNG ABSENDEN
   ================================================= */
bookingForm.onsubmit = e =>{
  e.preventDefault();

  const data = Object.fromEntries(new FormData(bookingForm));
  data.userId = currentUser ? currentUser.uid : "guest";
  data.paymentStatus = "pending"; // Stripe / PayPal Webhook

  db.collection("bookings").add(data);

  startProcess();
  alert("Danke! Deine Anfrage wurde gespeichert.");
  bookingForm.reset();
};

/* =================================================
   PROZESS ANIMATION
   ================================================= */
function startProcess(){
  const steps = document.querySelectorAll(".step");
  process.classList.remove("hidden");
  let i = 0;

  const interval = setInterval(()=>{
    if(i>0) steps[i-1].classList.remove("active");
    if(i < steps.length){
      steps[i].classList.add("active");
      i++;
    } else clearInterval(interval);
  }, 1200);
}

/* =================================================
   ADMIN – BUCHUNGEN & ZAHLUNGSSTATUS
   ================================================= */
db.collection("bookings").onSnapshot(snapshot=>{
  adminBookings.innerHTML = "";
  snapshot.forEach(doc=>{
    const b = doc.data();
    adminBookings.innerHTML += `
      <div class="card">
        <b>${b.name}</b> – ${b.type}<br>
        Budget: ${b.budget} €<br>
        Zahlung: <b>${b.paymentStatus}</b><br>
        <button onclick="
          db.collection('bookings')
          .doc('${doc.id}')
          .update({paymentStatus:'bezahlt'})
        ">Als bezahlt markieren</button>
      </div>
    `;
  });
});


function scrollToBooking(){
  document.getElementById("booking")
    .scrollIntoView({behavior:"smooth"});
}
</script>

</body>
</html>
