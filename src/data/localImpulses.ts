import type { ThoughtStatus, ThoughtType } from '../domain/types';

export function localImpulseFor(args: {
  types: ThoughtType[];
  status: ThoughtStatus;
  hasHypothesis: boolean;
}): string {
  if (args.types.includes('hypothesis') || args.hasHypothesis) {
    return 'Które założenie w tej myśli jest najcieńsze — i dałoby się je nazwać jednym zdaniem?';
  }
  if (args.types.includes('question')) {
    return 'Gdyby to pytanie miało dwa różne mechanizmy, czym by się różniły w obserwacji?';
  }
  if (args.types.includes('analogy')) {
    return 'Co w tej analogii jest podobieństwem rysunku, a co mogłoby być wspólnym procesem?';
  }
  if (args.types.includes('invention')) {
    return 'Jaki jest najmniejszy, całkowicie bezpieczny test pojęciowy tej idei — bez budowy i bez eksperymentu na sobie?';
  }
  if (args.types.includes('observation')) {
    return 'Jakie jedno „dlaczego właśnie tak?” zostaje, gdy odejmiesz nazwy i zostawisz mechanizm?';
  }
  if (args.status === 'paused') {
    return 'Nie trzeba wracać do całości. Który jeden fragment wciąż jest żywy?';
  }
  return 'Co w tym zapisie jest Twoją obserwacją, a co już interpretacją?';
}
