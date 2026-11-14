import { useState } from 'react';
import { updateProfile } from '../services/profile';

const slides = [
  {
    kicker: 'Witaj',
    title: 'To jest miejsce na Twoją myśl — nawet niedokończoną.',
    body: 'Neurotrino Lab nie jest listą zadań. Służy do chwytania obserwacji, pytań i hipotez, łączenia ich i wracania bez poczucia zaległości.',
  },
  {
    kicker: 'Autorstwo',
    title: 'Ty myślisz. Asystent, jeśli go włączysz, daje impuls.',
    body: 'Surowy zapis zostaje nietknięty. Propozycje AI są osobno oznaczone. Domyślnie nic nie wychodzi z urządzenia.',
  },
  {
    kicker: 'Prywatność',
    title: 'Dane zostają u Ciebie.',
    body: 'Notatki są w przeglądarce. Możesz je wyeksportować. Nie ma konta, feedu ani streaków.',
  },
];

export function Onboarding() {
  const [index, setIndex] = useState(0);
  const slide = slides[index]!;

  async function finish() {
    await updateProfile({ onboarded: true });
  }

  return (
    <div className="onboarding">
      <div className="onboarding-card card">
        <p className="kicker">{slide.kicker}</p>
        <h1>{slide.title}</h1>
        <p className="lede">{slide.body}</p>
        <div className="actions">
          {index < slides.length - 1 ? (
            <button className="btn primary" type="button" onClick={() => setIndex((i) => i + 1)}>
              Dalej
            </button>
          ) : (
            <button className="btn primary" type="button" onClick={finish}>
              Wejdź do laboratorium
            </button>
          )}
          <button className="btn ghost" type="button" onClick={finish}>
            Pomiń
          </button>
        </div>
      </div>
    </div>
  );
}
