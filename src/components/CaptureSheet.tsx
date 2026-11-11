import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { useNavigate } from 'react-router-dom';
import { mindStateHints, mindStateLabels } from '../domain/labels';
import type { MindState } from '../domain/types';
import { captureThoughts } from '../services/thoughts';
import { setMindState } from '../services/profile';

interface CaptureApi {
  open: (preset?: { text?: string; mind?: MindState }) => void;
  close: () => void;
}

const CaptureContext = createContext<CaptureApi>({ open: () => undefined, close: () => undefined });

export function useCapture(): CaptureApi {
  return useContext(CaptureContext);
}

export function CaptureProvider({ children }: { children: ReactNode }) {
  const [open, setOpen] = useState(false);
  const [text, setText] = useState('');
  const [mind, setMind] = useState<MindState>();
  const [busy, setBusy] = useState(false);
  const navigate = useNavigate();

  const api = useMemo<CaptureApi>(
    () => ({
      open: (preset) => {
        setText(preset?.text ?? '');
        setMind(preset?.mind);
        setOpen(true);
      },
      close: () => setOpen(false),
    }),
    [],
  );

  const save = useCallback(
    async (goToStudio: boolean) => {
      if (!text.trim() || busy) return;
      setBusy(true);
      try {
        if (mind) await setMindState(mind);
        const created = await captureThoughts({ text });
        const first = created[0];
        setText('');
        setOpen(false);
        if (goToStudio && first) navigate(`/mysli/${first.id}`);
        else if (first) navigate('/mysli');
      } finally {
        setBusy(false);
      }
    },
    [busy, mind, navigate, text],
  );

  return (
    <CaptureContext.Provider value={api}>
      {children}
      {open ? (
        <div className="modal-backdrop" role="presentation" onClick={() => setOpen(false)}>
          <div
            className="modal"
            role="dialog"
            aria-labelledby="capture-title"
            onClick={(e) => e.stopPropagation()}
          >
            <p className="kicker">Szybki zapis</p>
            <h2 id="capture-title">Co ostatnio chodzi Ci po głowie?</h2>
            <p className="muted">
              {mind ? mindStateHints[mind] : 'Jedno zdanie wystarczy. Tytuł i typ możesz dodać później.'}
            </p>
            <div className="chip-row" style={{ margin: '12px 0' }}>
              {(Object.keys(mindStateLabels) as MindState[]).map((key) => (
                <button
                  key={key}
                  type="button"
                  className="chip"
                  aria-pressed={mind === key}
                  onClick={() => setMind((current) => (current === key ? undefined : key))}
                >
                  {mindStateLabels[key]}
                </button>
              ))}
            </div>
            <textarea
              className="field"
              autoFocus
              rows={6}
              value={text}
              onChange={(e) => setText(e.target.value)}
              placeholder={
                mind === 'mgła'
                  ? 'Zastanawia mnie, jak…'
                  : mind === 'za_dużo_wątków'
                    ? 'Wątek 1\n\nWątek 2\n\nWątek 3'
                    : 'Obserwacja, pytanie, analogia, luźny strzęp…'
              }
            />
            <div className="actions">
              <button className="btn primary" type="button" disabled={!text.trim() || busy} onClick={() => save(true)}>
                Zapisz i otwórz
              </button>
              <button className="btn" type="button" disabled={!text.trim() || busy} onClick={() => save(false)}>
                Zapisz i zostaw
              </button>
              <button className="btn ghost" type="button" onClick={() => setOpen(false)}>
                Anuluj
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </CaptureContext.Provider>
  );
}
