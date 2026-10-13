<script>
// Constantes de módulo (no reactivas): se definen en un <script> normal, aparte del
// <script setup> de abajo, para que no se vuelvan a crear en cada instancia del
// componente — igual que en la versión original de React, donde vivían fuera del
// componente. Ambos bloques comparten el mismo módulo, así que el <script setup>
// puede usarlas directamente sin importarlas.
const PRESETS = {
  cylinder: { axis: 'y', tilt: -5, perspective: 2500, curve: 1, spread: 1, inward: false, billboard: false, backfaces: true, window: 0 },
  orbit: { axis: 'y', tilt: -16, perspective: 1500, curve: 0, spread: 1.45, inward: false, billboard: true, backfaces: false, window: 0 },
  wheel: { axis: 'x', tilt: 0, perspective: 1800, curve: 0, spread: 1, inward: false, billboard: false, backfaces: true, window: 1.7 },
  panorama: { axis: 'y', tilt: 0, perspective: 0, curve: 1, spread: 1, inward: true, billboard: false, backfaces: false, window: 0 },
}

const INTRO_LENGTH = { assemble: 1500, rise: 1400, spin: 1800, none: 0 }
const TILES = 8
const OVERLAP = 2.5
const DRAG_THRESHOLD = 5
const SPRING = 118
const SETTLE_SPEED = 9
const CAPTION_SPACE = 76
const SWAP_LENGTH = 700
const SWAP_SPREAD = 420
const TO_RAD = Math.PI / 180

const clamp = (value, min, max) => Math.min(max, Math.max(min, value))
const wrap = (degrees) => ((((degrees + 180) % 360) + 360) % 360) - 180
const easeOut = (t) => 1 - Math.pow(1 - t, 4)
const easeOutQuint = (t) => 1 - Math.pow(1 - t, 5)

const rotateX = (p, degrees) => {
  const r = degrees * TO_RAD
  const c = Math.cos(r)
  const s = Math.sin(r)
  return [p[0], p[1] * c - p[2] * s, p[1] * s + p[2] * c]
}

const rotateY = (p, degrees) => {
  const r = degrees * TO_RAD
  const c = Math.cos(r)
  const s = Math.sin(r)
  return [p[0] * c + p[2] * s, p[1], -p[0] * s + p[2] * c]
}
</script>

<script setup>
// Puerto a Vue 3 (Composition API) del componente "CircularCarousel" de React Bits.
// La física del carrusel (resorte, inercia, arrastre, parallax) es JavaScript puro y
// se mantiene casi línea por línea igual al original; lo que cambia es cómo se
// conecta con el framework:
//   - useState/useRef   -> ref()
//   - useMemo           -> computed()
//   - useEffect/useLayoutEffect -> onMounted() + watch()
// Como en Vue el setup() de un componente se ejecuta una sola vez (no en cada
// render, como en React), las funciones del bucle de animación no necesitan los
// "refs espejo" que React usaba solo para leer el valor más reciente dentro de un
// closure viejo: aquí basta leer directamente ready.value, settings.value, etc.
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  items: { type: Array, default: () => [] },
  preset: { type: String, default: 'cylinder' },
  intro: { type: String, default: 'rise' },
  cardWidth: { type: Number, default: 220 },
  aspectRatio: { type: Number, default: 1 },
  gap: { type: Number, default: 25 },
  curve: { type: Number, default: null },
  tilt: { type: Number, default: null },
  perspective: { type: Number, default: null },
  autoplay: { type: String, default: 'drift' },
  speed: { type: Number, default: 14 },
  interval: { type: Number, default: 3 },
  direction: { type: String, default: 'left' },
  draggable: { type: Boolean, default: true },
  momentum: { type: Number, default: 0.6 },
  snap: { type: Boolean, default: true },
  pauseOnHover: { type: Boolean, default: true },
  focusOnClick: { type: Boolean, default: true },
  parallax: { type: Number, default: 0.3 },
  stretch: { type: Number, default: 0.5 },
  depthFade: { type: Number, default: 0.55 },
  fadeColor: { type: String, default: '#000000' },
  innerShade: { type: Number, default: 0.6 },
  cornerRadius: { type: Number, default: 12 },
  captions: { type: Boolean, default: false },
})

const emit = defineEmits(['change', 'item-click'])

const list = ref(props.items)
const leaving = ref(null)
const active = ref(0)
const ready = ref(false)
const dragging = ref(false)
const reduced = ref(false)

const count = computed(() => list.value.length)
const shape = computed(() => (PRESETS[props.preset] ? props.preset : 'cylinder'))
const layout = computed(() => PRESETS[shape.value])
const axis = computed(() => layout.value.axis)
const tiltValue = computed(() => props.tilt ?? layout.value.tilt)
const curveValue = computed(() =>
  layout.value.billboard ? 0 : clamp(props.curve ?? layout.value.curve, 0, 1)
)

const cardW = computed(() => Math.max(40, props.cardWidth))
const cardH = computed(() => cardW.value / clamp(props.aspectRatio, 0.2, 5))
const along = computed(() => (axis.value === 'x' ? cardH.value : cardW.value))
const step = computed(() => 360 / Math.max(count.value, 1))

const radius = computed(() => {
  const n = Math.max(count.value, 3)
  const pitch = (along.value + props.gap) * layout.value.spread
  const chord = pitch / (2 * Math.sin(Math.PI / n))
  const arc = (n * pitch) / (2 * Math.PI)
  return Math.max(chord + (arc - chord) * curveValue.value, along.value * 0.6)
})

const tiles = computed(() => {
  const total = curveValue.value > 0.001 ? TILES : 1
  const length = along.value / total
  const bend = curveValue.value > 0.001 ? radius.value / curveValue.value : 0
  return Array.from({ length: total }, (_, index) => {
    const start = index * length - (index > 0 ? OVERLAP / 2 : 0)
    const end = (index + 1) * length + (index < total - 1 ? OVERLAP / 2 : 0)
    const center = (start + end) / 2 - along.value / 2
    const alpha = bend ? center / bend : 0
    const shift = bend ? bend * Math.sin(alpha) : center
    const sink = bend ? bend * (1 - Math.cos(alpha)) : 0
    const depth = layout.value.inward ? sink : -sink
    const turn = ((layout.value.inward ? -alpha : alpha) * 180) / Math.PI
    const move =
      axis.value === 'x'
        ? `translate3d(0px, ${shift}px, ${depth}px) rotateX(${-turn}deg)`
        : `translate3d(${shift}px, 0px, ${depth}px) rotateY(${turn}deg)`
    return { index, total, start, end, size: end - start, move }
  })
})

const settings = computed(() => ({
  count: count.value,
  step: step.value,
  radius: radius.value,
  layout: layout.value,
  axis: axis.value,
  tilt: tiltValue.value,
  perspective: layout.value.inward ? radius.value : props.perspective ?? layout.value.perspective,
  cardW: cardW.value,
  cardH: cardH.value,
  intro: reduced.value ? 'none' : props.intro in INTRO_LENGTH ? props.intro : 'rise',
  autoplay: reduced.value ? 'off' : props.autoplay,
  speed: props.speed,
  interval: Math.max(0.5, props.interval),
  draggable: props.draggable,
  momentum: clamp(props.momentum, 0, 1),
  snap: props.snap,
  pauseOnHover: props.pauseOnHover,
  parallax: reduced.value ? 0 : clamp(props.parallax, 0, 1),
  stretch: reduced.value ? 0 : clamp(props.stretch, 0, 1),
  depthFade: clamp(props.depthFade, 0, 1),
  captions: props.captions,
  reduced: reduced.value,
}))

const dragSign = computed(() => (layout.value.inward ? -1 : 1))
const directionSign = computed(() => (props.direction === 'right' ? 1 : -1) * dragSign.value)

const rootStyle = computed(() => ({
  '--cc-fade': props.fadeColor,
  '--cc-radius': `${Math.max(0, props.cornerRadius)}px`,
  '--cc-inner': (1 - clamp(props.innerShade, 0, 1)).toFixed(3),
}))

const current = computed(() => list.value[active.value] || list.value[0])
const label = computed(() =>
  current.value ? current.value.title || current.value.alt || `Image ${active.value + 1}` : ''
)
const activeDigits = computed(() => String(active.value + 1).padStart(2, '0').split(''))

const rootEl = ref(null)
const stageEl = ref(null)
const cameraEl = ref(null)
const ringEl = ref(null)
// Arreglo mutable simple (no un ref de Vue): se llena/lee a mano dentro del bucle
// de animación, igual que el useRef([]) original — no necesita ser reactivo porque
// nada en la plantilla lee su contenido directamente.
const cardEls = []
function setCardRef(el, index) {
  if (el) cardEls[index] = el
}

// Estado "físico" del carrusel: también mutable y no reactivo a propósito. Se
// actualiza en cada fotograma de animación directamente sobre el DOM (card.style...),
// sin pasar por el sistema de reactividad de Vue, por rendimiento — igual que el
// original evita re-renders de React en cada frame.
const state = {
  angle: 0,
  velocity: 0,
  target: null,
  dir: 0,
  press: null,
  drag: false,
  hover: false,
  pointer: { inside: false, x: 0, y: 0 },
  yaw: 0,
  pitch: 0,
  intro: null,
  introDone: false,
  holdUntil: 0,
  stepAt: 0,
  suppressClick: false,
  wheelTimer: 0,
  fit: 1,
  shift: 0,
  drop: 0,
  last: 0,
}

let raf = 0
let visible = true
let resizeObserver = null
let intersectionObserver = null
let onVisibility = () => {}
let reducedQuery = null
let leavingTimer = null
let wake = () => {}
let measure = () => {}

function nearest(angle) {
  return Math.round(angle / settings.value.step) * settings.value.step
}

watch(directionSign, (value) => {
  state.dir = value
  wake()
}, { immediate: true })

function updateReduced() {
  reduced.value = !!reducedQuery?.matches
}

const sourcesKey = computed(() => props.items.map((item) => item.src).join('|'))

watch(sourcesKey, () => {
  const next = props.items
  const smooth = ready.value && next.length === list.value.length
  if (!smooth) {
    ready.value = false
    leaving.value = null
    list.value = next
  }
  const sources = next.slice(0, 16).map((item) => item.src)
  const load = (src) =>
    new Promise((resolve) => {
      const image = new Image()
      image.decoding = 'async'
      image.onload = () => (image.decode ? image.decode().then(resolve, resolve) : resolve())
      image.onerror = resolve
      image.src = src
    })
  const timeout = new Promise((resolve) => setTimeout(resolve, 2400))
  Promise.race([Promise.all(sources.map(load)), timeout]).then(() => {
    if (smooth) {
      const previous = list.value
      if (!settings.value.reduced && previous !== next) {
        leaving.value = {
          id: (leaving.value?.id || 0) + 1,
          items: previous,
          delays: previous.map(
            (_, index) => (Math.abs(wrap(index * settings.value.step + state.angle)) / 180) * SWAP_SPREAD
          ),
        }
      }
      list.value = next
      wake()
      return
    }
    state.introDone = false
    state.intro = null
    ready.value = true
    wake()
  })
}, { immediate: true })

watch(leaving, (value) => {
  clearTimeout(leavingTimer)
  if (!value) return
  leavingTimer = setTimeout(() => {
    leaving.value = null
  }, SWAP_LENGTH + SWAP_SPREAD + 80)
})

watch([radius, cardW, cardH, tiltValue, () => props.perspective, () => props.preset, () => props.captions, count], () => {
  measure()
  wake()
})

watch([list, leaving, dragging], () => wake())

onMounted(() => {
  reducedQuery = window.matchMedia?.('(prefers-reduced-motion: reduce)') || null
  updateReduced()
  reducedQuery?.addEventListener?.('change', updateReduced)

  const root = rootEl.value
  const stage = stageEl.value
  const camera = cameraEl.value
  const ring = ringEl.value
  if (!root || !stage || !camera || !ring) return

  measure = () => {
    const s = settings.value
    const rect = root.getBoundingClientRect()
    if (!rect.width || !rect.height) return
    const room = s.captions ? CAPTION_SPACE : 0
    const width = rect.width * 0.94
    const height = (rect.height - room) * 0.92
    const P = s.perspective
    let minX = Infinity
    let maxX = -Infinity
    let minY = Infinity
    let maxY = -Infinity
    if (s.layout.inward) {
      minX = -width / 2
      maxX = width / 2
      minY = -s.cardH / 2
      maxY = s.cardH / 2
    } else {
      const corners = [
        [-s.cardW / 2, -s.cardH / 2],
        [s.cardW / 2, -s.cardH / 2],
        [-s.cardW / 2, s.cardH / 2],
        [s.cardW / 2, s.cardH / 2],
      ]
      const limit = s.layout.window ? s.layout.window * s.step : 180
      for (let a = -limit; a <= limit; a += limit / 24) {
        for (const [cx, cy] of corners) {
          let p
          if (s.axis === 'x') {
            p = rotateY(rotateX([cx, cy, s.radius], -a), s.tilt)
          } else if (s.layout.billboard) {
            const c = rotateY([0, 0, s.radius], a)
            p = rotateX([c[0] + cx, cy, c[2]], s.tilt)
          } else {
            p = rotateX(rotateY([cx, cy, s.radius], a), s.tilt)
          }
          p = [p[0], p[1], p[2] - s.radius]
          if (p[2] >= P * 0.95) continue
          const k = P / (P - p[2])
          minX = Math.min(minX, p[0] * k)
          maxX = Math.max(maxX, p[0] * k)
          minY = Math.min(minY, p[1] * k)
          maxY = Math.max(maxY, p[1] * k)
        }
      }
    }
    const spanX = Math.max(maxX - minX, 1)
    const spanY = Math.max(maxY - minY, 1)
    const fit = Math.min(1, width / spanX, height / spanY)
    state.fit = fit
    state.shift = -((minY + maxY) / 2) * fit - room / 2
    state.drop = s.axis === 'x' ? (rect.width / fit) * 0.55 + s.cardW : (rect.height / fit) * 0.55 + s.cardH
    stage.style.perspective = `${P}px`
    stage.style.transform = `translate3d(0, ${state.shift}px, 0) scale(${fit})`
  }

  const introCard = (elapsed, landing) => {
    if (!state.intro) return { radius: 1, lift: 0 }
    const type = state.intro.type
    const reach = Math.abs(wrap(landing + state.angle))
    if (type === 'assemble') {
      const delay = (reach / 180) * 420
      const p = easeOut(clamp((elapsed - delay) / 1080, 0, 1))
      return { radius: 1 + 0.6 * (1 - p), lift: 0 }
    }
    if (type === 'rise') {
      const delay = (reach / 180) * 480
      const p = easeOutQuint(clamp((elapsed - delay) / 900, 0, 1))
      return { radius: 1, lift: (1 - p) * state.drop }
    }
    if (type === 'spin') {
      const p = easeOut(clamp(elapsed / INTRO_LENGTH.spin, 0, 1))
      return { radius: 1 + 0.28 * (1 - p), lift: 0 }
    }
    return { radius: 1, lift: 0 }
  }

  const advance = (s, dt, now) => {
    if (!state.introDone && ready.value) {
      if (!state.intro) {
        if (s.intro === 'none') state.introDone = true
        else state.intro = { type: s.intro, start: now }
      }
      if (state.intro && now - state.intro.start >= INTRO_LENGTH[state.intro.type]) {
        state.intro = null
        state.introDone = true
      }
    }

    const paused = (s.pauseOnHover && state.hover) || state.drag || now < state.holdUntil
    const cruise = s.autoplay === 'drift' && !paused && !state.intro ? s.speed * state.dir : 0
    let busy = Boolean(state.intro) || state.drag

    if (state.drag || state.intro) {
      state.velocity = state.drag ? state.velocity : 0
    } else if (state.target !== null) {
      let remaining = dt
      const damping = 2 * Math.sqrt(SPRING)
      while (remaining > 0) {
        const h = Math.min(remaining, 1 / 240)
        const accel = SPRING * (state.target - state.angle) - damping * state.velocity
        state.velocity += accel * h
        state.angle += state.velocity * h
        remaining -= h
      }
      if (Math.abs(state.target - state.angle) < 0.004 && Math.abs(state.velocity) < 0.03) {
        state.angle = state.target
        state.velocity = 0
        state.target = null
      }
      busy = true
    } else {
      const tau = 0.18 + s.momentum * 1.5
      state.velocity += (cruise - state.velocity) * (1 - Math.exp(-dt / tau))
      state.angle += state.velocity * dt
      if (cruise === 0 && s.snap && Math.abs(state.velocity) < SETTLE_SPEED) {
        state.target = nearest(state.angle)
      }
      busy = busy || cruise !== 0 || Math.abs(state.velocity) > 0.01 || state.target !== null
    }

    if (s.autoplay === 'step' && !paused && !state.intro && state.introDone) {
      if (!state.stepAt) state.stepAt = now + s.interval * 1000
      if (now >= state.stepAt) {
        state.target = (state.target ?? nearest(state.angle)) + s.step * state.dir
        state.stepAt = now + s.interval * 1000
      }
      busy = true
    } else {
      state.stepAt = 0
    }

    if (now < state.holdUntil) busy = true

    const ease = 1 - Math.exp(-dt / 0.35)
    const aimYaw = state.pointer.inside ? state.pointer.x * s.parallax * 9 : 0
    const aimPitch = state.pointer.inside ? -state.pointer.y * s.parallax * 6 : 0
    state.yaw += (aimYaw - state.yaw) * ease
    state.pitch += (aimPitch - state.pitch) * ease
    if (Math.abs(aimYaw - state.yaw) > 0.01 || Math.abs(aimPitch - state.pitch) > 0.01) busy = true

    return busy
  }

  const render = (s, now) => {
    const elapsed = state.intro ? now - state.intro.start : 0
    const swell = 1 + s.stretch * 0.12 * Math.min(1, Math.abs(state.velocity) / 420)
    let spinOffset = 0
    if (state.intro?.type === 'spin') {
      const p = easeOut(clamp(elapsed / INTRO_LENGTH.spin, 0, 1))
      spinOffset = -300 * state.dir * (1 - p)
    } else if (state.intro?.type === 'assemble') {
      const p = easeOut(clamp(elapsed / INTRO_LENGTH.assemble, 0, 1))
      spinOffset = -32 * state.dir * (1 - p)
    }
    const angle = state.angle + spinOffset
    const R = s.radius * swell

    if (s.axis === 'x') {
      camera.style.transform = `translate3d(0, 0, ${-R}px) rotateY(${s.tilt + state.yaw}deg) rotateX(${state.pitch}deg)`
      ring.style.transform = `rotateX(${-angle}deg)`
    } else if (s.layout.inward) {
      camera.style.transform = `translate3d(0, 0, ${s.perspective - 1}px) rotateX(${s.tilt + state.pitch}deg) rotateY(${state.yaw}deg)`
      ring.style.transform = `rotateY(${angle}deg)`
    } else {
      camera.style.transform = `translate3d(0, 0, ${-R}px) rotateX(${s.tilt + state.pitch}deg) rotateY(${state.yaw}deg)`
      ring.style.transform = `rotateY(${angle}deg)`
    }

    for (let index = 0; index < s.count; index++) {
      const card = cardEls[index]
      if (!card) continue
      const base = index * s.step
      const mod = introCard(elapsed, base)
      const r = R * mod.radius
      let transform
      if (s.axis === 'x') {
        transform = `rotateX(${-base}deg) translateZ(${r}px)`
      } else if (s.layout.inward) {
        transform = `rotateY(${base}deg) translateZ(${-r}px)`
      } else {
        transform = `rotateY(${base}deg) translateZ(${r}px)`
        if (s.layout.billboard) transform += ` rotateY(${-(base + angle)}deg)`
      }
      if (mod.lift) transform += s.axis === 'x' ? ` translateX(${mod.lift}px)` : ` translateY(${mod.lift}px)`
      card.style.transform = transform

      const world = wrap(base + angle)
      const facing = Math.cos(world * TO_RAD)
      if (s.layout.inward) card.style.visibility = Math.abs(world) > 86 ? 'hidden' : ''
      const fade = s.depthFade * Math.pow((1 - facing) / 2, 1.25)
      card.style.setProperty('--cc-depth', fade.toFixed(3))
    }

    const index = ((Math.round(-state.angle / s.step) % s.count) + s.count) % s.count || 0
    if (index !== active.value) {
      active.value = index
      emit('change', index)
    }
  }

  const frame = (now) => {
    raf = 0
    const s = settings.value
    const dt = state.last ? Math.min((now - state.last) / 1000, 0.05) : 1 / 60
    state.last = now
    const busy = advance(s, dt, now)
    render(s, now)
    if (busy && visible && !document.hidden) raf = requestAnimationFrame(frame)
    else state.last = 0
  }

  wake = () => {
    if (!raf && visible && !document.hidden) raf = requestAnimationFrame(frame)
  }

  onVisibility = () => {
    if (document.hidden) {
      cancelAnimationFrame(raf)
      raf = 0
      state.last = 0
    } else wake()
  }

  resizeObserver = new ResizeObserver(() => {
    measure()
    wake()
  })
  resizeObserver.observe(root)

  intersectionObserver = new IntersectionObserver(([entry]) => {
    visible = entry.isIntersecting
    if (visible) wake()
    else {
      cancelAnimationFrame(raf)
      raf = 0
      state.last = 0
    }
  })
  intersectionObserver.observe(root)

  document.addEventListener('visibilitychange', onVisibility)

  measure()
  render(settings.value, performance.now())
  wake()
})

onUnmounted(() => {
  cancelAnimationFrame(raf)
  resizeObserver?.disconnect()
  intersectionObserver?.disconnect()
  clearTimeout(state.wheelTimer)
  clearTimeout(leavingTimer)
  document.removeEventListener('visibilitychange', onVisibility)
  reducedQuery?.removeEventListener?.('change', updateReduced)
})

function updatePointer(event) {
  const rect = rootEl.value.getBoundingClientRect()
  const pointer = state.pointer
  pointer.x = clamp(((event.clientX - rect.left) / rect.width) * 2 - 1, -1, 1)
  pointer.y = clamp(((event.clientY - rect.top) / rect.height) * 2 - 1, -1, 1)
}

function focusIndex(index) {
  const s = settings.value
  let target = -index * s.step
  target += 360 * Math.round((state.angle - target) / 360)
  state.target = target
  state.holdUntil = performance.now() + 2800
  wake()
}

function stepBy(delta) {
  const s = settings.value
  const base = state.target ?? Math.round(state.angle / s.step) * s.step
  state.target = base - delta * s.step * (s.layout.inward ? -1 : 1)
  state.holdUntil = performance.now() + 2800
  wake()
}

function handlePointerDown(event) {
  state.suppressClick = false
  if (!props.draggable || event.button !== 0) return
  state.press = {
    id: event.pointerId,
    x: event.clientX,
    y: event.clientY,
    angle: state.angle,
    moved: false,
    origin: 0,
    samples: [{ time: performance.now(), angle: state.angle }],
  }
}

function handlePointerMove(event) {
  if (event.pointerType === 'mouse') {
    state.pointer.inside = true
    updatePointer(event)
  }
  const press = state.press
  if (!press || press.id !== event.pointerId) {
    wake()
    return
  }
  const s = settings.value
  const delta = s.axis === 'x' ? event.clientY - press.y : event.clientX - press.x
  const cross = s.axis === 'x' ? event.clientX - press.x : event.clientY - press.y
  if (!press.moved) {
    if (Math.abs(delta) < DRAG_THRESHOLD) return
    if (Math.abs(cross) > Math.abs(delta) * 1.2 && event.pointerType !== 'mouse') {
      state.press = null
      return
    }
    press.moved = true
    press.origin = delta
    state.drag = true
    state.target = null
    state.velocity = 0
    dragging.value = true
    try {
      rootEl.value.setPointerCapture(event.pointerId)
    } catch {}
  }
  const perPixel = 180 / (Math.PI * s.radius * state.fit)
  state.angle = press.angle + (delta - press.origin) * perPixel * (s.layout.inward ? -1 : 1)
  const now = performance.now()
  press.samples.push({ time: now, angle: state.angle })
  while (press.samples.length > 2 && now - press.samples[0].time > 110) press.samples.shift()
  wake()
}

function releasePointer(event) {
  const press = state.press
  if (!press || press.id !== event.pointerId) return
  state.press = null
  if (!press.moved) return
  state.drag = false
  dragging.value = false
  state.suppressClick = true
  const s = settings.value
  const first = press.samples[0]
  const last = press.samples[press.samples.length - 1]
  const span = (last.time - first.time) / 1000
  const velocity = span > 0.008 ? clamp((last.angle - first.angle) / span, -1400, 1400) : 0
  state.velocity = velocity
  if (Math.abs(velocity) > 60) state.dir = Math.sign(velocity)
  const coasting = s.autoplay === 'drift' && !(s.pauseOnHover && state.hover && event.pointerType === 'mouse')
  if (s.snap && !coasting) {
    const tau = 0.18 + s.momentum * 1.5
    state.target = Math.round((state.angle + velocity * tau * 0.55) / s.step) * s.step
  }
  wake()
}

function handlePointerEnter(event) {
  if (event.pointerType !== 'mouse') return
  state.hover = true
  wake()
}

function handlePointerLeave(event) {
  if (event.pointerType === 'mouse') {
    state.hover = false
    state.pointer.inside = false
  }
  wake()
}

function handleClick(event) {
  if (state.suppressClick) {
    state.suppressClick = false
    return
  }
  const card = event.target.closest?.('[data-cc-index]')
  if (!card) return
  const index = Number(card.getAttribute('data-cc-index'))
  if (props.focusOnClick) focusIndex(index)
  emit('item-click', list.value[index], index)
}

function handleKeyDown(event) {
  const forward = axis.value === 'x' ? 'ArrowDown' : 'ArrowRight'
  const backward = axis.value === 'x' ? 'ArrowUp' : 'ArrowLeft'
  if (event.key === forward) stepBy(1)
  else if (event.key === backward) stepBy(-1)
  else if (event.key === 'Home') focusIndex(0)
  else if (event.key === 'End') focusIndex(count.value - 1)
  else if (event.key === 'Enter' || event.key === ' ') emit('item-click', list.value[active.value], active.value)
  else return
  event.preventDefault()
}

function onWheel(event) {
  const s = settings.value
  if (!s.draggable) return
  const delta = Math.abs(event.deltaX) > Math.abs(event.deltaY) ? event.deltaX : 0
  if (!delta) return
  event.preventDefault()
  const perPixel = 180 / (Math.PI * s.radius * state.fit)
  state.target = null
  state.angle -= delta * perPixel * (s.layout.inward ? -1 : 1)
  state.velocity = -delta * perPixel * (s.layout.inward ? -1 : 1) * 30
  state.holdUntil = performance.now() + 1600
  clearTimeout(state.wheelTimer)
  state.wheelTimer = setTimeout(() => {
    if (settings.value.snap) state.target = nearest(state.angle + state.velocity * 0.12)
    wake()
  }, 140)
  wake()
}

// Las siguientes funciones reemplazan a "renderTile" de la versión React: en vez de
// devolver JSX, devuelven objetos de estilo que la plantilla ata con :style. No
// cambian en cada fotograma de la animación (eso lo hace directamente el bucle de
// arriba sobre cardEls), solo cuando cambian props/lista/tiles.
function tileStyle(tile, back) {
  const size = tile.size
  const box =
    axis.value === 'x'
      ? { left: `${-cardW.value / 2}px`, top: `${-size / 2}px`, width: `${cardW.value}px`, height: `${size}px` }
      : { left: `${-size / 2}px`, top: `${-cardH.value / 2}px`, width: `${size}px`, height: `${cardH.value}px` }
  const flip = axis.value === 'x' ? ' rotateX(180deg)' : ' rotateY(180deg)'
  box.transform = tile.move + (back ? flip : '')
  return box
}

function frameStyle(tile, back) {
  const strip = back ? tile.total - 1 - tile.index : tile.index
  const first = strip === 0
  const last = strip === tile.total - 1
  const r = 'var(--cc-radius)'
  const borderRadius =
    axis.value === 'x'
      ? `${first ? r : 0} ${first ? r : 0} ${last ? r : 0} ${last ? r : 0}`
      : `${first ? r : 0} ${last ? r : 0} ${last ? r : 0} ${first ? r : 0}`
  return { height: `${axis.value === 'x' ? tile.size : cardH.value}px`, borderRadius }
}

function photoStyle(tile, back) {
  const offset = back ? along.value - tile.end : tile.start
  return axis.value === 'x'
    ? { left: '0px', top: `${-offset}px`, width: `${cardW.value}px`, height: `${cardH.value}px` }
    : { left: `${-offset}px`, top: '0px', width: `${cardW.value}px`, height: `${cardH.value}px` }
}

function leavingPhotoStyle(tile, back, delay) {
  return { ...photoStyle(tile, back), animationDelay: `${Math.round(delay)}ms` }
}
</script>

<template>
  <div
    ref="rootEl"
    class="circular-carousel"
    :style="rootStyle"
    role="region"
    aria-roledescription="carousel"
    aria-label="Carrusel de imágenes"
    tabindex="0"
    :data-axis="axis"
    :data-shape="shape"
    :data-ready="ready ? '' : null"
    :data-draggable="draggable ? '' : null"
    :data-dragging="dragging ? '' : null"
    @pointerdown="handlePointerDown"
    @pointermove="handlePointerMove"
    @pointerup="releasePointer"
    @pointercancel="releasePointer"
    @pointerenter="handlePointerEnter"
    @pointerleave="handlePointerLeave"
    @click="handleClick"
    @keydown="handleKeyDown"
    @wheel="onWheel"
  >
    <div class="circular-carousel__view">
      <div ref="stageEl" class="circular-carousel__stage">
        <div ref="cameraEl" class="circular-carousel__camera">
          <div ref="ringEl" class="circular-carousel__ring">
            <div
              v-for="(item, index) in list"
              :key="index"
              :ref="(el) => setCardRef(el, index)"
              class="circular-carousel__card"
              :data-cc-index="index"
              role="group"
              aria-roledescription="slide"
              :aria-label="`${item.title || item.alt || 'Imagen ' + (index + 1)}, ${index + 1} de ${count}`"
            >
              <div
                v-for="tile in tiles"
                :key="`f${tile.index}`"
                class="circular-carousel__tile"
                :style="tileStyle(tile, false)"
                aria-hidden="true"
              >
                <div class="circular-carousel__frame" :style="frameStyle(tile, false)">
                  <img
                    class="circular-carousel__photo"
                    :src="item.src"
                    alt=""
                    draggable="false"
                    decoding="async"
                    :style="photoStyle(tile, false)"
                  />
                  <img
                    v-if="leaving && leaving.items[index]"
                    :key="`leaving-f-${leaving.id}`"
                    class="circular-carousel__photo circular-carousel__photo--leaving"
                    :src="leaving.items[index].src"
                    alt=""
                    draggable="false"
                    decoding="async"
                    :style="leavingPhotoStyle(tile, false, leaving.delays[index])"
                  />
                  <div class="circular-carousel__shade"></div>
                </div>
              </div>

              <template v-if="layout.backfaces">
                <div
                  v-for="tile in tiles"
                  :key="`b${tile.index}`"
                  class="circular-carousel__tile"
                  :style="tileStyle(tile, true)"
                  aria-hidden="true"
                >
                  <div class="circular-carousel__frame" :style="frameStyle(tile, true)">
                    <img
                      class="circular-carousel__photo"
                      :src="item.src"
                      alt=""
                      draggable="false"
                      decoding="async"
                      :style="photoStyle(tile, true)"
                    />
                    <img
                      v-if="leaving && leaving.items[index]"
                      :key="`leaving-b-${leaving.id}`"
                      class="circular-carousel__photo circular-carousel__photo--leaving"
                      :src="leaving.items[index].src"
                      alt=""
                      draggable="false"
                      decoding="async"
                      :style="leavingPhotoStyle(tile, true, leaving.delays[index])"
                    />
                    <div class="circular-carousel__inner"></div>
                    <div class="circular-carousel__shade"></div>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="captions && current" class="circular-carousel__caption" aria-hidden="true">
      <span :key="active" class="circular-carousel__title">
        {{ current.title || current.alt }}
        <span v-if="current.subtitle" class="circular-carousel__subtitle">{{ current.subtitle }}</span>
      </span>
      <span class="circular-carousel__count">
        <span class="circular-carousel__digits">
          <span v-for="(digit, i) in activeDigits" :key="i" class="circular-carousel__digit">
            <span class="circular-carousel__reel" :style="{ transform: `translateY(${-Number(digit) * 10}%)` }">
              <span v-for="n in 10" :key="n">{{ n - 1 }}</span>
            </span>
          </span>
        </span>
        <span class="circular-carousel__slash">/</span>
        <span>{{ String(count).padStart(2, '0') }}</span>
      </span>
    </div>

    <div class="circular-carousel__live" aria-live="polite" aria-atomic="true">
      {{ label }}, {{ active + 1 }} de {{ count }}
    </div>
  </div>
</template>

<style scoped>
.circular-carousel {
  --cc-fade: #000000;
  --cc-radius: 12px;
  --cc-inner: 0.4;
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  outline: none;
  touch-action: pan-y;
  user-select: none;
  -webkit-user-select: none;
  -webkit-tap-highlight-color: transparent;
}

.circular-carousel[data-axis='x'] {
  touch-action: pan-x;
}

.circular-carousel[data-draggable] {
  cursor: grab;
}

.circular-carousel[data-dragging] {
  cursor: grabbing;
}

.circular-carousel:focus-visible {
  box-shadow: inset 0 0 0 2px rgba(128, 128, 140, 0.55);
}

.circular-carousel__view {
  position: absolute;
  inset: 0;
}

.circular-carousel[data-shape='wheel'] .circular-carousel__view {
  -webkit-mask-image: linear-gradient(to bottom, transparent, #000000 18%, #000000 82%, transparent);
  mask-image: linear-gradient(to bottom, transparent, #000000 18%, #000000 82%, transparent);
}

.circular-carousel[data-shape='panorama'] .circular-carousel__view {
  -webkit-mask-image: linear-gradient(to right, transparent, #000000 12%, #000000 88%, transparent);
  mask-image: linear-gradient(to right, transparent, #000000 12%, #000000 88%, transparent);
}

.circular-carousel__stage {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.45s ease;
}

.circular-carousel[data-ready] .circular-carousel__stage {
  opacity: 1;
}

.circular-carousel__camera,
.circular-carousel__ring,
.circular-carousel__card {
  width: 0;
  height: 0;
  transform-style: preserve-3d;
}

.circular-carousel__camera {
  position: relative;
}

.circular-carousel__ring,
.circular-carousel__card {
  position: absolute;
  left: 0;
  top: 0;
}

.circular-carousel__tile {
  position: absolute;
  outline: 1px solid transparent;
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
}

.circular-carousel__frame {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  overflow: hidden;
}

.circular-carousel__photo {
  position: absolute;
  display: block;
  max-width: none;
  object-fit: cover;
  pointer-events: none;
  user-select: none;
  -webkit-user-drag: none;
}

.circular-carousel__photo--leaving {
  animation: circular-carousel-leave 700ms cubic-bezier(0.33, 1, 0.68, 1) both;
}

.circular-carousel__shade,
.circular-carousel__inner {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.circular-carousel__shade {
  background: var(--cc-fade);
  opacity: var(--cc-depth, 0);
}

.circular-carousel__inner {
  background: #000000;
  opacity: var(--cc-inner);
}

.circular-carousel__caption {
  /* z-index explícito: el stage usa transform-style: preserve-3d para el aro de
     tarjetas, y eso le da su propia capa de composición en Chromium que pinta por
     encima de hermanos posteriores en el DOM aunque no tengan transformaciones 3D.
     Sin esto, el caption queda tapado por el aro aunque aparece después en el HTML. */
  z-index: 1;
  position: absolute;
  left: 0;
  right: 0;
  bottom: 22px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 0 16px;
  color: inherit;
  text-align: center;
  pointer-events: none;
  animation: circular-carousel-reveal 900ms ease both;
}

.circular-carousel__title {
  display: flex;
  max-width: 100%;
  flex-direction: column;
  align-items: center;
  font-size: 15px;
  font-weight: 500;
  line-height: 1.35;
  animation: circular-carousel-title 520ms cubic-bezier(0.22, 1, 0.36, 1);
}

.circular-carousel__subtitle {
  font-weight: 400;
  opacity: 0.6;
}

.circular-carousel__count {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  font-variant-numeric: tabular-nums;
  line-height: 1;
  opacity: 0.5;
}

.circular-carousel__slash {
  opacity: 0.6;
}

.circular-carousel__digits {
  display: inline-flex;
}

.circular-carousel__digit {
  display: inline-block;
  height: 1em;
  overflow: hidden;
}

.circular-carousel__reel {
  display: flex;
  flex-direction: column;
  transition: transform 600ms cubic-bezier(0.22, 1, 0.36, 1);
}

.circular-carousel__reel > span {
  height: 1em;
}

.circular-carousel__live {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
}

@keyframes circular-carousel-title {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes circular-carousel-leave {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}

@keyframes circular-carousel-reveal {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@media (prefers-reduced-motion: reduce) {
  .circular-carousel__stage,
  .circular-carousel__reel {
    transition: none;
  }

  .circular-carousel__caption,
  .circular-carousel__title {
    animation: none;
  }
}
</style>
