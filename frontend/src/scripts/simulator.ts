type Step = {
  number: number;
  state: string;
  readSymbol: string;
  headPosition: number;
  tape: string[];
  stack: string[];
  action: string;
};

type Simulation = {
  level: number;
  title: string;
  input: string;
  accepted: boolean;
  result: string;
  message: string;
  steps: Step[];
};

type Module = {
  title: string;
  description: string;
  language: string;
  examples: string[];
  hint: string;
  rule: string;
  machine: string;
  concept: string;
  footer: string;
  notation: string;
  notationDescription: string;
  placeholder: string;
  maxLength: number;
};

type Story = { title: string; explanation: string; reason: string };

const modules: Record<number, Module> = {
  3: {
    title: 'Autómata finito determinista',
    description: 'Reconoce patrones con una cantidad finita de estados.',
    language: 'L = a*b*',
    examples: ['aaabbb', 'abba', 'bbb', 'ε'],
    hint: 'Letras permitidas: a y b. La entrada vacía también se acepta.',
    rule: 'a*b*',
    machine: 'Diagrama de estados',
    concept: 'Sigue el estado que se ilumina al leer cada letra. q₀ permite a; después de entrar en q₁, solo se permite b.',
    footer: 'MEMORIA · ESTADOS FINITOS',
    notation: 'Σ = {a, b}<br />F = {q₀, q₁}',
    notationDescription: 'a*b* significa cero o más a, seguidas de cero o más b.',
    placeholder: 'Por ejemplo: aaabbb',
    maxLength: 80,
  },
  2: {
    title: 'Autómata de pila',
    description: 'Usa memoria LIFO para contar y comparar bloques de símbolos.',
    language: 'L = {aⁿbⁿ}',
    examples: ['aabb', 'ab', 'aab', 'abab', 'ε'],
    hint: 'Escribe primero las a y luego la misma cantidad de b. La entrada vacía se acepta.',
    rule: 'aⁿbⁿ · n ≥ 0',
    machine: 'Estado y pila',
    concept: 'Observa cómo cada a añade una marca a la pila y cada b retira una. El orden contrario no se acepta.',
    footer: 'MEMORIA · PILA',
    notation: 'Σ = {a, b}<br />Γ = {Z₀, A}',
    notationDescription: 'La pila empieza y termina con Z₀ cuando hay la misma cantidad de a y b.',
    placeholder: 'Por ejemplo: aabb',
    maxLength: 80,
  },
  1: {
    title: 'Máquina linealmente acotada',
    description: 'Marca símbolos en una cinta cuyos límites dependen del tamaño de la entrada.',
    language: 'L = {aⁿbⁿcⁿ}',
    examples: ['aabbcc', 'abc', 'aabcc', 'abbc', 'ε'],
    hint: 'Escribe a, luego la misma cantidad de b y c. Máximo: 24 símbolos.',
    rule: 'aⁿbⁿcⁿ · n ≥ 0',
    machine: 'Cinta acotada y cabezal',
    concept: 'El cabezal busca una a, una b y una c, las marca y vuelve al inicio. Los bordes ⊢ y ⊣ limitan la cinta.',
    footer: 'MEMORIA · CINTA ACOTADA',
    notation: 'Σ = {a, b, c}<br />Γ = {a, b, c, X, Y, Z}',
    notationDescription: 'X, Y y Z indican qué letras ya se emparejaron; n debe ser igual en los tres bloques.',
    placeholder: 'Por ejemplo: aabbcc',
    maxLength: 24,
  },
  0: {
    title: 'Máquina de Turing',
    description: 'Ejecuta una suma unaria leyendo y escribiendo sobre su cinta.',
    language: '1ⁿ + 1ᵐ',
    examples: ['11+111', '1+1', '111+11', '1+'],
    hint: 'Cada 1 vale una unidad. Escribe dos grupos no vacíos separados por +.',
    rule: '1ⁿ + 1ᵐ → 1ⁿ⁺ᵐ',
    machine: 'Cinta, cabezal y estado',
    concept: 'Mira cómo la máquina reemplaza + por 1 y borra una marca del final. El resultado es la suma de los dos grupos.',
    footer: 'MEMORIA · CINTA EXTENDIBLE',
    notation: 'Σ = {1, +}<br />Γ = {1, +, □}',
    notationDescription: 'La cinta contiene unos, el separador + y espacios vacíos □.',
    placeholder: 'Por ejemplo: 11+111',
    maxLength: 41,
  },
};

const $ = <T extends HTMLElement>(id: string) => document.getElementById(id) as T;
const input = $<HTMLInputElement>('input-value');
const visualization = $('visualization');
const API = import.meta.env.PUBLIC_API_URL ?? 'http://localhost:8080';
let level = 3;
let simulation: Simulation | null = null;
let current = 0;
let timer: number | undefined;

export function startSimulator() {
  document.querySelectorAll<HTMLButtonElement>('.nav-item').forEach((button) => {
    button.addEventListener('click', () => selectLevel(Number(button.dataset.level)));
  });

  $('run-button').addEventListener('click', run);
  input.addEventListener('keydown', (event) => {
    if (event.key === 'Enter') run();
  });
  input.addEventListener('input', handleInputChange);
  $('step-forward').addEventListener('click', () => showStep(current + 1));
  $('step-back').addEventListener('click', () => showStep(current - 1));
  $('reset-button').addEventListener('click', () => {
    pausePlayback();
    showStep(0);
  });
  $('play-button').addEventListener('click', togglePlayback);
  $('step-progress').addEventListener('input', (event) => {
    showStep(Number((event.currentTarget as HTMLInputElement).value));
  });

  selectLevel(3);
}

function selectLevel(nextLevel: number) {
  pausePlayback();
  level = nextLevel;
  simulation = null;
  current = 0;
  const module = modules[level];

  document.querySelectorAll('.nav-item').forEach((node) => {
    const button = node as HTMLElement;
    const active = Number(button.dataset.level) === level;
    button.classList.toggle('active', active);
    if (active) button.setAttribute('aria-current', 'page');
    else button.removeAttribute('aria-current');
  });

  $('crumb').textContent = `NIVEL ${level}`;
  $('level-kicker').innerHTML = `NIVEL ${level} <span>·</span> TIPO ${level}`;
  $('level-chip').textContent = String(level);
  $('module-title').textContent = module.title;
  $('module-description').textContent = module.description;
  $('language-tag').textContent = module.language;
  $('input-hint').textContent = module.hint;
  $('rule-value').textContent = module.rule;
  $('machine-heading').textContent = module.machine;
  $('concept-text').textContent = module.concept;
  $('concept-footer').textContent = module.footer;
  $('notation').innerHTML = module.notation;
  $('notation-description').textContent = module.notationDescription;
  input.placeholder = module.placeholder;
  input.maxLength = module.maxLength;
  input.value = '';
  updateInputLabel();

  const examples = $('examples-list');
  examples.replaceChildren();
  module.examples.forEach((example) => {
    const button = document.createElement('button');
    button.className = 'example-chip';
    button.textContent = example === 'ε' ? 'ε · vacía' : example;
    button.title = example === 'ε' ? 'Cargar la cadena vacía' : `Cargar el ejemplo ${example}`;
    button.addEventListener('click', () => {
      input.value = example === 'ε' ? '' : example;
      handleInputChange();
      input.focus();
    });
    examples.append(button);
  });

  $<HTMLButtonElement>('run-button').disabled = false;
  setResult('idle', '¿La acepta?', 'Escribe una cadena o carga un ejemplo y pulsa Simular.');
  $('step-counter').textContent = 'Sin pasos';
  resetWalkthrough();
  visualization.innerHTML = '<div class="empty-machine"><span class="empty-icon">◎</span><b>El recorrido aparecerá aquí</b><span>Primero carga un ejemplo o escribe una cadena.</span></div>';
  setControlsEnabled(false);
}

function handleInputChange() {
  updateInputLabel();
  if (!simulation) return;

  pausePlayback();
  simulation = null;
  current = 0;
  setResult('idle', 'Entrada modificada', 'Vuelve a pulsar Simular para ver el recorrido de esta cadena.');
  $('step-counter').textContent = 'Sin pasos';
  resetWalkthrough('La entrada cambió. Ejecuta la simulación para actualizar el recorrido.');
  visualization.innerHTML = '<div class="empty-machine"><span class="empty-icon">↻</span><b>Recorrido pendiente</b><span>Pulsa Simular para evaluar la nueva cadena.</span></div>';
  setControlsEnabled(false);
}

function updateInputLabel() {
  $('symbol-count').textContent = `${input.value.length} ${input.value.length === 1 ? 'símbolo' : 'símbolos'}`;
  $('input-status').textContent = input.value.length === 0
    ? 'Cadena vacía (ε)'
    : `Lista para simular · ${input.value.length} ${input.value.length === 1 ? 'símbolo' : 'símbolos'}`;
}

function resetWalkthrough(message = 'Luego podrás ver qué símbolo está leyendo la máquina y qué cambia en su memoria.') {
  $('walkthrough-icon').textContent = '1';
  $('step-title').textContent = 'Primero, ejecuta una cadena';
  $('step-explanation').textContent = message;
  $('step-reason').textContent = 'Usa «Siguiente» para avanzar de uno en uno o «Reproducir» para verlo en movimiento.';
  $('fact-read').textContent = '—';
  $('fact-state').textContent = '—';
  $('fact-memory').textContent = '—';
}

async function run() {
  pausePlayback();
  const rawInput = input.value;
  const runButton = $<HTMLButtonElement>('run-button');
  runButton.disabled = true;
  input.disabled = true;
  document.querySelectorAll<HTMLButtonElement>('.example-chip, .nav-item').forEach((button) => {
    button.disabled = true;
  });
  runButton.innerHTML = 'Calculando <span>…</span>';
  setResult('idle', 'Preparando simulación', 'La máquina está creando el recorrido paso a paso.');
  setWalkthrough('Preparando la máquina…', 'Cada paso mostrará qué lee, cómo cambia de estado y qué ocurre con su memoria.', 'Cuando termine, podrás avanzar o reproducir la ejecución.');

  try {
    const response = await fetch(`${API}/api/simulations/${level}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ input: rawInput }),
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.error ?? 'No se pudo ejecutar la simulación.');

    simulation = data as Simulation;
    current = 0;
    renderMachine();
    const progress = $<HTMLInputElement>('step-progress');
    progress.max = String(simulation.steps.length - 1);
    progress.value = '0';
    setControlsEnabled(true);
    showStep(0);
    const outcome = simulation.level === 0 ? `Resultado: ${simulation.result}` : simulation.accepted ? 'Cadena aceptada' : 'Cadena rechazada';
    setResult(simulation.accepted ? 'accepted' : 'rejected', outcome, simulation.message);
  } catch (error) {
    simulation = null;
    setControlsEnabled(false);
    const message = error instanceof Error ? error.message : 'No se pudo conectar con la API.';
    setResult('rejected', 'No se pudo simular', message);
    setWalkthrough('La máquina no pudo ejecutarse', message, 'Comprueba que el backend Java esté activo en localhost:8080.');
    visualization.innerHTML = '<div class="empty-machine"><span class="empty-icon">!</span><b>No se pudo conectar con la API</b><span>Comprueba que el backend Java esté activo en localhost:8080.</span></div>';
  } finally {
    input.disabled = false;
    document.querySelectorAll<HTMLButtonElement>('.example-chip, .nav-item').forEach((button) => {
      button.disabled = false;
    });
    runButton.disabled = false;
    runButton.innerHTML = 'Simular <span>↗</span>';
  }
}

function setResult(kind: string, title: string, message: string) {
  $('result-tag').className = `result-tag ${kind}`;
  $('result-tag').textContent = kind === 'accepted' ? 'ACEPTADA' : kind === 'rejected' ? 'RECHAZADA' : 'LISTO';
  $('result-title').textContent = title;
  $('result-message').textContent = message;
}

function setWalkthrough(title: string, explanation: string, reason: string) {
  $('step-title').textContent = title;
  $('step-explanation').textContent = explanation;
  $('step-reason').textContent = reason;
}

function renderMachine() {
  if (!simulation) return;
  if (level === 3) renderDfa();
  else if (level === 2) renderStack();
  else renderTape();
}

function renderDfa() {
  const tokens = simulation!.input.split('').map((symbol, index) =>
    `<span class="dfa-token" data-index="${index}">${symbol}</span>`).join('') || '<span class="epsilon-token">ε · cadena vacía</span>';
  visualization.innerHTML = `
    <div class="dfa-view">
      <div class="dfa-input"><span>CADENA DE ENTRADA</span><div class="dfa-tokens">${tokens}</div></div>
      <div class="dfa">
        <svg class="dfa-routes" viewBox="0 0 600 180" role="img" aria-label="Diagrama de transiciones del autómata">
          <defs><marker id="arrowhead" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto"><path d="M0,0 L0,6 L7,3 z" /></marker></defs>
          <path class="route start-route" d="M20 90 H88" marker-end="url(#arrowhead)" />
          <path class="route edge-q0-a" d="M125 58 C95 8 195 8 165 58" marker-end="url(#arrowhead)" />
          <path class="route edge-q0-b" d="M181 90 H318" marker-end="url(#arrowhead)" />
          <path class="route edge-q1-b" d="M325 58 C295 8 395 8 365 58" marker-end="url(#arrowhead)" />
          <path class="route edge-q1-a" d="M390 111 C424 155 463 145 490 111" marker-end="url(#arrowhead)" />
          <path class="route edge-q0-other" d="M180 111 C260 170 412 169 490 118" marker-end="url(#arrowhead)" />
          <path class="route edge-qd-any" d="M500 58 C475 12 560 12 540 58" marker-end="url(#arrowhead)" />
        </svg>
        <div class="state accept" data-state="q₀"><span>q₀</span><small>inicio · acepta</small></div>
        <div class="state accept" data-state="q₁"><span>q₁</span><small>acepta</small></div>
        <div class="state trap" data-state="qᵈ"><span>qᵈ</span><small>rechaza</small></div>
        <div class="dfa-edge edge-aa">a</div><div class="dfa-edge edge-ab">b</div><div class="dfa-edge edge-bb">b</div><div class="dfa-edge edge-dead">a</div>
        <div class="dfa-edge edge-other">otro</div><div class="dfa-edge edge-trap">a, b, otro</div>
      </div>
      <p class="diagram-key"><span>↻</span> Flecha circular: lee el símbolo y permanece en el mismo estado.</p>
    </div>`;
}

function renderStack() {
  visualization.innerHTML = '<div class="stack-view"><div class="stack-flow"><span class="flow-label">CADENA</span><div id="stack-input" class="input-tokens"></div></div><div class="stack-machine"><div id="stack-state" class="stack-state">qₐ</div><div class="stack-arrow">↓</div><div class="stack-visual"><span class="stack-cap">TOPE · ÚLTIMO EN ENTRAR</span><div id="stack-cells"></div><span class="stack-base">BASE</span></div></div><div class="stack-note">Cada <b>a</b> añade una A. Cada <b>b</b> debe quitar una A.</div></div>';
  const tokens = $('stack-input');
  if (!simulation!.input.length) {
    tokens.innerHTML = '<span class="epsilon-token">ε · cadena vacía</span>';
    return;
  }
  simulation!.input.split('').forEach((symbol, index) => {
    const token = document.createElement('span');
    token.textContent = symbol;
    token.dataset.index = String(index);
    tokens.append(token);
  });
}

function renderTape() {
  visualization.innerHTML = '<div class="tape-view"><div class="tape-state" id="tape-state">ESTADO q₀</div><div class="tape-caption">CINTA DE TRABAJO <span id="tape-bounds">DELIMITADA</span></div><div class="tape-scroll"><div id="tape-cells" class="tape-cells"></div><div id="tape-head" class="tape-head">▼</div></div><div class="tape-alphabet" id="tape-alphabet"></div></div>';
  const alphabet = $('tape-alphabet');
  (level === 1 ? ['⊢', 'a', 'b', 'c', 'X', 'Y', 'Z', '⊣'] : ['□', '1', '+']).forEach((symbol) => {
    const chip = document.createElement('span');
    chip.textContent = symbol;
    alphabet.append(chip);
  });
}

function showStep(index: number) {
  if (!simulation) return;
  current = Math.max(0, Math.min(index, simulation.steps.length - 1));
  const step = simulation.steps[current];
  $('step-counter').textContent = `Paso ${current + 1} de ${simulation.steps.length}`;
  $<HTMLInputElement>('step-progress').value = String(current);
  syncControls();

  const story = narrate(step);
  $('walkthrough-icon').textContent = String(current + 1);
  setWalkthrough(story.title, story.explanation, story.reason);
  $('fact-read').textContent = current === 0 && level >= 2
    ? 'Inicio'
    : step.readSymbol === 'ε' ? 'Fin' : `«${step.readSymbol}»`;
  $('fact-state').textContent = step.state;
  $('fact-memory').textContent = memorySummary(step);

  if (level === 3) showDfaStep(step);
  else if (level === 2) showStackStep(step);
  else showTapeStep(step);
}

function showDfaStep(step: Step) {
  document.querySelectorAll('.state').forEach((state) => {
    state.classList.toggle('current', state.getAttribute('data-state') === step.state);
  });
  document.querySelectorAll<HTMLElement>('.dfa-token').forEach((token) => {
    const tokenIndex = Number(token.dataset.index);
    token.classList.toggle('read-token', tokenIndex < current - 1);
    token.classList.toggle('current-token', tokenIndex === current - 1 && current <= simulation!.input.length);
  });
  document.querySelectorAll('.route').forEach((route) => route.classList.remove('route-active'));
  if (current === 0) document.querySelector('.start-route')?.classList.add('route-active');
  const previousState = simulation!.steps[Math.max(0, current - 1)].state;
  const edge = transitionEdge(previousState, step.readSymbol);
  if (edge) document.querySelector(`.${edge}`)?.classList.add('route-active');
}

function showStackStep(step: Step) {
  $('stack-state').textContent = step.state;
  const cells = $('stack-cells');
  cells.replaceChildren();
  step.stack.forEach((symbol, index) => {
    const cell = document.createElement('span');
    cell.textContent = symbol;
    cell.className = index === step.stack.length - 1 ? 'stack-top-cell' : '';
    cells.append(cell);
  });
  document.querySelectorAll<HTMLElement>('.input-tokens [data-index]').forEach((token) => {
    const tokenIndex = Number(token.dataset.index);
    token.classList.toggle('consumed', tokenIndex < current - 1);
    token.classList.toggle('current-token', tokenIndex === current - 1 && current <= simulation!.input.length);
  });
}

function showTapeStep(step: Step) {
  const cells = $('tape-cells');
  cells.replaceChildren();
  step.tape.forEach((symbol, index) => {
    const cell = document.createElement('span');
    cell.textContent = symbol;
    cell.className = 'tape-cell';
    if (index === step.headPosition) cell.classList.add('head-cell');
    cells.append(cell);
  });
  $('tape-state').textContent = `ESTADO ${step.state} · LEE ${step.readSymbol}`;
  $('tape-head').style.transform = `translateX(${Math.max(0, step.headPosition) * 57}px)`;
  const scroller = document.querySelector('.tape-scroll') as HTMLElement;
  scroller.scrollLeft = Math.max(0, step.headPosition * 57 - scroller.clientWidth / 2);
}

function transitionEdge(state: string, symbol: string) {
  if (state === 'q₀' && symbol === 'a') return 'edge-q0-a';
  if (state === 'q₀' && symbol === 'b') return 'edge-q0-b';
  if (state === 'q₀' && !['a', 'b', 'ε'].includes(symbol)) return 'edge-q0-other';
  if (state === 'q₁' && symbol === 'b') return 'edge-q1-b';
  if (state === 'q₁' && symbol === 'a') return 'edge-q1-a';
  if (state === 'qᵈ' && symbol !== 'ε') return 'edge-qd-any';
  return '';
}

function narrate(step: Step): Story {
  const last = current === simulation!.steps.length - 1;
  const symbol = step.readSymbol;
  const previous = simulation!.steps[Math.max(0, current - 1)];

  if (current === 0) {
    const starts: Record<number, Story> = {
      3: { title: 'La máquina empieza en q₀', explanation: 'q₀ es el estado inicial. Todavía no ha leído ninguna letra.', reason: 'En el siguiente paso leerá el primer símbolo de la cadena.' },
      2: { title: 'La pila empieza con Z₀', explanation: 'Z₀ es el marcador de fondo. Cada a añadirá una A; cada b tendrá que quitar una.', reason: 'La cadena se acepta si se consumen todos los símbolos y la pila vuelve a Z₀.' },
      1: { title: 'El cabezal comienza junto a ⊢', explanation: 'La cinta está delimitada. El cabezal buscará una a, una b y una c para emparejarlas.', reason: 'Cada pasada marca un grupo y luego vuelve al inicio de la cinta.' },
      0: { title: 'El cabezal busca el signo +', explanation: `La cinta contiene «${simulation!.input}». El separador divide los dos sumandos unarios.`, reason: 'Al encontrar +, la máquina lo cambiará por una marca 1.' },
    };
    return starts[level];
  }

  if (last) {
    if (level === 0) {
      return {
        title: `La suma es ${simulation!.result}`,
        explanation: `Al unir los dos grupos y borrar la marca sobrante, quedan ${simulation!.result.length} unos.`,
        reason: 'La máquina terminó el cálculo y se detuvo en qf.',
      };
    }
    if (simulation!.accepted) {
      const explanation = level === 2
        ? 'Se leyó toda la cadena y solo queda Z₀: había la misma cantidad de a y b.'
        : level === 1
          ? 'Todos los símbolos se emparejaron y marcaron. No quedaron letras pendientes.'
          : 'La entrada terminó en q₀ o q₁, que son estados de aceptación.';
      return { title: 'La cadena es aceptada', explanation, reason: 'La máquina procesó la cadena completa y cumplió su condición de aceptación.' };
    }
    const explanation = level === 2
      ? 'Quedaron marcas en la pila o el orden de las letras no cumple aⁿbⁿ.'
      : 'La máquina llegó a un estado de rechazo o quedaron símbolos sin emparejar.';
    return { title: 'La cadena es rechazada', explanation, reason: 'La máquina terminó de revisar la entrada y no se cumplió la condición de aceptación.' };
  }

  if (level === 3) {
    if (step.state === 'qᵈ') {
      const explanation = previous.state === 'qᵈ'
        ? `La máquina ya llegó a qᵈ; leer «${symbol}» no puede sacarla del estado de rechazo.`
        : previous.state === 'q₁' && symbol === 'a'
          ? 'La máquina ya había empezado el bloque de b. Encontrar una a después rompe el orden a*b*.'
          : `«${symbol}» no pertenece al alfabeto {a, b}, así que no hay una transición válida.`;
      return { title: `Lee «${symbol}» y llega a qᵈ`, explanation, reason: 'qᵈ es el estado trampa: cualquier símbolo restante mantiene el rechazo.' };
    }
    if (previous.state === 'q₀' && symbol === 'a') {
      return { title: 'Lee «a» y se queda en q₀', explanation: 'q₀ permite leer todas las a iniciales; no necesita contar cuántas han pasado.', reason: 'La flecha circular indica que el estado se mantiene.' };
    }
    if (previous.state === 'q₀' && symbol === 'b') {
      return { title: 'Lee «b» y cambia a q₁', explanation: 'Aquí comienza el bloque de b. Desde ahora solo pueden aparecer más b.', reason: 'La transición q₀ → q₁ separa los dos bloques del lenguaje.' };
    }
    return { title: 'Lee «b» y se queda en q₁', explanation: 'La máquina ya está leyendo el bloque final de b y puede continuar en q₁.', reason: 'q₁ acepta: la cadena podría terminar aquí.' };
  }

  if (level === 2) {
    if (step.state === 'qᵣ') {
      let explanation = `«${symbol}» no pertenece al alfabeto {a, b}.`;
      if (previous.state === 'qᵦ' && symbol === 'a') explanation = 'Ya empezó el bloque de b; volver a leer a rompe el orden requerido.';
      else if (symbol === 'b' && previous.stack.length <= 1) explanation = 'La pila no tiene una A para retirar. Hay más b que a.';
      return { title: `Lee «${symbol}»: no hay transición válida`, explanation, reason: 'El autómata de pila rechaza si no puede emparejar la letra con su memoria.' };
    }
    if (symbol === 'a') {
      const count = step.stack.length - 1;
      return { title: 'Lee «a» y apila una A', explanation: `La pila guarda ${count} ${count === 1 ? 'marca' : 'marcas'}. Cada A representa una a que debe emparejarse.`, reason: 'Las a se guardan para compararlas después con las b.' };
    }
    const remaining = step.stack.length - 1;
    return { title: 'Lee «b» y desapila una A', explanation: `Después de retirar esta marca, quedan ${remaining} ${remaining === 1 ? 'A' : 'A'} en la pila.`, reason: 'Cada b debe encontrar una A disponible; si no queda ninguna, la cadena se rechaza.' };
  }

  if (step.action.includes('Marca un a')) {
    return { title: 'Marca una a como X', explanation: 'Esta a queda emparejada con una b y una c. X recuerda que ya se usó.', reason: 'La máquina repetirá la búsqueda hasta que no queden a sin marcar.' };
  }
  if (step.action.includes('Marca un b')) {
    return { title: 'Marca una b como Y', explanation: 'Encontró una b disponible para la a marcada. Y señala que esta b ya se emparejó.', reason: 'Ahora debe encontrar una c disponible.' };
  }
  if (step.action.includes('Marca un c')) {
    return { title: 'Marca una c como Z', explanation: 'Encontró una c para completar el grupo a-b-c de esta pasada.', reason: 'Después vuelve al inicio para emparejar el siguiente grupo.' };
  }
  if (step.state === 'q→') {
    const explanation = symbol === '⊣'
      ? 'El cabezal llegó al borde derecho sin encontrar la letra que necesitaba.'
      : `Revisa «${symbol}» para encontrar la siguiente letra sin marcar.`;
    return { title: `Busca el siguiente símbolo y avanza`, explanation, reason: 'El cabezal recorre la cinta una celda por paso y no cruza ⊣.' };
  }
  if (step.state === 'q←') {
    return { title: 'Vuelve hacia el inicio', explanation: 'El cabezal regresa celda por celda para empezar otra pasada.', reason: 'El borde ⊢ marca hasta dónde puede regresar.' };
  }

  if (level === 0) {
    if (step.action.includes('Escribe')) return { title: 'Reemplaza + por 1', explanation: 'Al cambiar el separador por una marca, los dos grupos quedan unidos.', reason: 'Después avanzará al final y quitará una marca para compensar este reemplazo.' };
    if (step.action.includes('Avanza')) return { title: 'Avanza por el segundo sumando', explanation: 'El cabezal pasa sobre cada 1 hasta llegar al espacio blanco al final de la cinta.', reason: 'Necesita encontrar el último 1 para terminar la suma.' };
    if (step.action.includes('Borra')) return { title: 'Borra la última marca', explanation: 'El + se convirtió en un 1 extra; borrar el último 1 deja exactamente la suma.', reason: 'La cinta se detiene con el resultado unario.' };
  }

  return { title: 'La máquina actualiza su configuración', explanation: step.action, reason: 'Observa el estado activo, el símbolo leído y el contenido de la memoria.' };
}

function memorySummary(step: Step) {
  if (level === 3) return step.state === 'qᵈ' ? 'estado trampa' : 'solo estado';
  if (level === 2) return `${step.stack.length - 1} ${step.stack.length - 1 === 1 ? 'marca' : 'marcas'} en pila`;
  if (level === 1) return `cabezal · celda ${step.headPosition}`;
  return `${step.tape.filter((cell) => cell === '1').length} unos en cinta`;
}

function togglePlayback() {
  if (timer) {
    pausePlayback();
    return;
  }
  if (!simulation) return;
  if (current >= simulation.steps.length - 1) showStep(0);
  timer = window.setInterval(() => {
    if (current >= simulation!.steps.length - 1) {
      pausePlayback();
      return;
    }
    showStep(current + 1);
  }, Number(($('speed-select') as HTMLSelectElement).value));
  $('play-button').querySelector('span')!.textContent = 'Ⅱ';
  $('play-button').querySelector('small')!.textContent = 'Pausar';
  $('play-button').setAttribute('aria-label', 'Pausar reproducción');
}

function pausePlayback() {
  if (timer) {
    window.clearInterval(timer);
    timer = undefined;
  }
  const button = $('play-button');
  button.querySelector('span')!.textContent = '▶';
  button.querySelector('small')!.textContent = 'Reproducir';
  button.setAttribute('aria-label', 'Reproducir');
}

function setControlsEnabled(enabled: boolean) {
  ['reset-button', 'step-back', 'play-button', 'step-forward', 'step-progress'].forEach((id) => {
    const element = $(id);
    if (element instanceof HTMLButtonElement || element instanceof HTMLInputElement) element.disabled = !enabled;
  });
  syncControls();
}

function syncControls() {
  if (!simulation) return;
  ($('reset-button') as HTMLButtonElement).disabled = current === 0;
  ($('step-back') as HTMLButtonElement).disabled = current === 0;
  ($('step-forward') as HTMLButtonElement).disabled = current === simulation.steps.length - 1;
  ($('step-progress') as HTMLInputElement).disabled = simulation.steps.length < 2;
}
