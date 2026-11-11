package lab.neurotrino.app.domain

object SparksCatalog {
    val items: List<Spark> = listOf(
        Spark(
            id = "spark-oddball-time",
            title = "Nowość i poczucie czasu",
            hook = "Nowy odcinek drogi potrafi „rozciągnąć” godzinę, a powrót tą samą trasą ją skraca.",
            explanation = "Układ nerwowy silniej koduje zdarzenia odbiegające od przewidywania. Gęstszy zapis w pamięci sprawia, że wstecznie odcinek wydaje się dłuższy. To opis zjawiska psychologicznego — nie fizyczny „zegar” w mózgu.",
            deeper = "Dopamina i uwaga modulują nowość, ale subiektywny czas ma kilka warstw: okno percepcji, tempo zdarzeń, pamięć epizodyczna i nastrój. Hipoteza, że liczba zdarzeń neuronalnych nadaje tempo, jest propozycją do sprawdzenia — nie konsensusem.",
            question = "Gdybyś miał odróżnić tempo zdarzeń od nowości zdarzeń, które z nich bardziej zmienia Twoje poczucie godziny?",
            domains = listOf("neurobiologia", "percepcja", "pamięć"),
            sourceNote = "Efekt nowości i zniekształcenie retrospektywnego czasu są opisywane w psychologii poznawczej; jednolity zegar zdarzeń pozostaje hipotezą.",
            kind = "observation",
            generatedBy = "catalog",
            why = "Łączy się z Twoim zainteresowaniem percepcją czasu i hipotezą zegara neuronalnego — bez mieszania ich w jeden fakt.",
        ),
        Spark(
            id = "spark-photon-opsin",
            title = "Foton, który staje się sygnałem",
            hook = "Światło nie „wchodzi do umysłu”. Kończy się na zmianie kształtu białka.",
            explanation = "Foton o odpowiedniej energii może zostać pochłonięty przez retinal związany z opsyną. Zmiana konformacji uruchamia kaskadę w fotoreceptorze. Widzenie zaczyna się jako chemia, nie jako obraz.",
            deeper = "Różne opsyny mają inne maksima absorpcji — stąd barwa jako rozróżnienie długości fali, nie jako właściwość fotonu „na zewnątrz”.",
            question = "W którym miejscu tej kaskady kończy się fizyka światła, a zaczyna Twoja interpretacja „widzę”?",
            domains = listOf("percepcja", "neurobiologia", "fizyka"),
            sourceNote = "Transdukcja w opsynach to dobrze opisany mechanizm biologiczny.",
            kind = "mechanism",
            generatedBy = "catalog",
            why = "Zwykłe patrzenie na przedmiot — mechanizm, który lubisz rozkładać.",
        ),
        Spark(
            id = "spark-analogy-limit",
            title = "Podobieństwo nie jest mechanizmem",
            hook = "Dwie rzeczy mogą wyglądać podobnie i nie mieć ze sobą nic wspólnego pod spodem.",
            explanation = "Sieć neuronowa, rzeczna i społeczna dzielą słowo „sieć”. To może być analogia strukturalna albo tylko metafora. Wspólny mechanizm wymaga tego samego rodzaju procesu przyczynowego.",
            question = "Które Twoje ostatnie połączenie jest analogią, a które naprawdę wskazuje ten sam rodzaj procesu?",
            domains = listOf("epistemologia", "filozofia"),
            kind = "bridge",
            generatedBy = "catalog",
            why = "To narzędzie, którym oznaczasz relacje na mapie: mechanizm / analogia / metafora.",
        ),
        Spark(
            id = "spark-open-time",
            title = "Czego nauka nie domyka w subiektywnym czasie",
            hook = "Potrafimy zmierzyć czas reakcji. Nadal nie mamy pełnego modelu „jak długo to trwało dla mnie”.",
            explanation = "Istnieją modele zegarów wewnętrznych i kodowania w pamięci. Żaden nie jest kompletnym opisem doświadczenia trwania. To uczciwie otwarte pytanie.",
            deeper = "Rozróżnij: dyskryminację krótkich interwałów, poczucie tempa w chwili, ocenę minionej godziny. Mogą zależeć od różnych obwodów.",
            question = "Którą z tych trzech warstw Twoja hipoteza naprawdę chce wyjaśnić jako pierwszą?",
            domains = listOf("świadomość", "neurobiologia"),
            sourceNote = "Otwartość dotyczy jednolitego mechanizmu doświadczenia czasu, nie samego istnienia efektów nowości.",
            kind = "open_question",
            generatedBy = "catalog",
            why = "Otwarty problem naukowy — nie luka marketingowa.",
        ),
        Spark(
            id = "spark-science-faith",
            title = "Dwa rodzaje pytań, jeden stół",
            hook = "Nie każde ważne pytanie da się rozstrzygnąć tym samym narzędziem.",
            explanation = "Metoda naukowa dobrze testuje twierdzenia empiryczne. Pytania metafizyczne i teologiczne można serio rozważać, ale nie stają się empiryczne przez analogię do fizyki.",
            question = "Które Twoje pytanie o początek jest empiryczne, a które pozostaje filozoficzne nawet po idealnym eksperymencie?",
            domains = listOf("filozofia", "kosmologia", "nauka i wiara"),
            kind = "open_question",
            generatedBy = "catalog",
            why = "Miejsce na naukę i wiarę obok siebie, bez udawania, że jedno udowodniło drugie.",
        ),
        Spark(
            id = "spark-prediction-error",
            title = "Mózg jako maszyna pomyłek",
            hook = "Uczenie często zaczyna się nie od bodźca, lecz od różnicy między oczekiwaniem a światem.",
            explanation = "W wielu modelach układ nerwowy przewiduje wejście i aktualizuje się, gdy predykcja zawodzi. To nie znaczy, że „żyjesz w symulacji”. Znaczy, że percepcja jest częściowo konstrukcją korygowaną przez błąd.",
            question = "Jakie zwykłe zjawisko wokół Ciebie jest teraz interesujące głównie dlatego, że złamało oczekiwanie?",
            domains = listOf("neurobiologia", "percepcja"),
            sourceNote = "Ramą jest błąd predykcji — model roboczy, nie jedyna teoria umysłu.",
            kind = "mechanism",
            generatedBy = "catalog",
            why = "Mechanizm pod zwyczajnym zaskoczeniem.",
        ),
        Spark(
            id = "spark-myelin",
            title = "Mielina: izolacja czy coś więcej?",
            hook = "Owijka wokół aksonu przyspiesza sygnał, ale porównanie do kabla elektrycznego ma granicę.",
            explanation = "Osłonka mielinowa zwiększa opór poprzeczny i zmniejsza pojemność błony, umożliwiając przewodzenie skokowe. W nerwie „izolacja” jest żywa i metabolicznie kosztowna.",
            question = "Gdzie w Twoich pomysłach „przewodzenie” jest metaforą, a gdzie naprawdę chodzi o timing?",
            domains = listOf("neurobiologia", "inżynieria"),
            sourceNote = "Przewodzenie skokowe i rola mieliny są ustalonym mechanizmem neurofizjologicznym.",
            kind = "bridge",
            generatedBy = "catalog",
            why = "Ćwiczenie na granicy analogii i mechanizmu — inżynieria spotyka biologie.",
        ),
        Spark(
            id = "spark-seed",
            title = "Nasiono, które nie „wie” dokąd leci",
            hook = "Trawa nie planuje podróży. Korzysta z kształtów, które fizyka nagradza.",
            explanation = "Nasiona przemieszczają się przez wiatr, zwierzęta, wodę albo wystrzał mechaniczny. Kształt, masa i kleistość to rozwiązania transportu, nie ozdoby.",
            question = "Jaki zwykły przedmiot obok Ciebie ma kształt, który zdradza, do jakiego środowiska został dopasowany?",
            domains = listOf("biologia", "ewolucja"),
            kind = "observation",
            generatedBy = "catalog",
            why = "Rozkładanie zwyczajnego zjawiska na mechanizm — Twój ulubiony trop.",
        ),
    )

    fun pick(thoughts: List<Thought>, reading: List<ReadingItem>, avoid: Set<String>): Spark {
        val resume = thoughts
            .filter { it.status in setOf(ThoughtStatus.paused, ThoughtStatus.exploring, ThoughtStatus.seed) }
            .minByOrNull { it.updatedAt }
        val resumeSpark = resume?.let {
            Spark(
                id = "resume-${it.id}",
                title = "Wątek, który czeka bez presji",
                hook = it.title.ifBlank { "Niedokończona myśl" },
                explanation = (it.content.ifBlank { it.rawText }).take(360),
                question = "Jaki jeden szczegół z tej notatki wciąż jest żywy — nawet jeśli reszta ostygła?",
                domains = it.tags.take(3),
                kind = "resume",
                generatedBy = "local",
                linkedThoughtIds = listOf(it.id),
                why = "To Twoja własna, odłożona myśl — nie nowa ciekawostka.",
            )
        }
        val book = reading.firstOrNull { it.status == ReadingStatus.exploring || it.status == ReadingStatus.paused }
        val bookSpark = book?.let {
            Spark(
                id = "book-${it.id}",
                title = "Książka jako iskra: ${it.title}",
                hook = it.progressNote.ifBlank { "Nie musisz czytać rozdziału. Wystarczy jedno własne pytanie." },
                explanation = "${it.author} — ${it.title}. Lektura jest zasobem, nie obowiązkiem.",
                question = "Jaki jeden obraz z tej książki (jeśli pamiętasz) łączy się z czymś, co ostatnio zauważyłeś?",
                domains = listOf("lektura"),
                kind = "reading",
                generatedBy = "local",
                why = "Masz tę książkę na półce i nie dokończyłeś jej — bez winy.",
            )
        }
        val pool = items.filter { it.id !in avoid }.toMutableList()
        if (resumeSpark != null) pool += resumeSpark
        if (bookSpark != null) pool += bookSpark
        return pool.randomOrNull() ?: items.first()
    }

    fun body(spark: Spark): String = buildString {
        append(spark.hook)
        append("\n\n")
        append(spark.explanation)
        if (spark.deeper.isNotBlank()) {
            append("\n\nMechanizm: ")
            append(spark.deeper)
        }
        append("\n\nPytanie: ")
        append(spark.question)
    }

    fun localImpulse(types: List<ThoughtType>, status: ThoughtStatus, hasHypothesis: Boolean): String = when {
        types.contains(ThoughtType.hypothesis) || hasHypothesis ->
            "Które założenie w tej myśli jest najcieńsze — i dałoby się je nazwać jednym zdaniem?"
        types.contains(ThoughtType.question) ->
            "Gdyby to pytanie miało dwa różne mechanizmy, czym by się różniły w obserwacji?"
        types.contains(ThoughtType.analogy) ->
            "Co w tej analogii jest podobieństwem rysunku, a co mogłoby być wspólnym procesem?"
        types.contains(ThoughtType.invention) ->
            "Jaki jest najmniejszy, całkowicie bezpieczny test pojęciowy tej idei — bez budowy i bez eksperymentu na sobie?"
        types.contains(ThoughtType.observation) ->
            "Jakie jedno „dlaczego właśnie tak?” zostaje, gdy odejmiesz nazwy i zostawisz mechanizm?"
        status == ThoughtStatus.paused ->
            "Nie trzeba wracać do całości. Który jeden fragment wciąż jest żywy?"
        else -> "Co w tym zapisie jest Twoją obserwacją, a co już interpretacją?"
    }
}
