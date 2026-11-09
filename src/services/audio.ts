class ZenAudioEngine {
  private ctx: AudioContext | null = null
  public soundEnabled: boolean = true
  public hapticsEnabled: boolean = true

  private getContext(): AudioContext {
    if (!this.ctx) {
      const AudioCtx = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext
      this.ctx = new AudioCtx()
    }
    if (this.ctx.state === 'suspended') {
      this.ctx.resume()
    }
    return this.ctx
  }

  private vibrate(ms: number) {
    if (this.hapticsEnabled && typeof navigator !== 'undefined' && navigator.vibrate) {
      navigator.vibrate(ms)
    }
  }

  // 1. Warm sine droplet tap (523.25 Hz C5)
  tap() {
    if (!this.soundEnabled) {
      this.vibrate(8)
      return
    }
    const ctx = this.getContext()
    const now = ctx.currentTime
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()

    osc.type = 'sine'
    osc.frequency.setValueAtTime(523.25, now)

    gain.gain.setValueAtTime(0, now)
    gain.gain.linearRampToValueAtTime(0.35, now + 0.003)
    gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.09)

    osc.connect(gain)
    gain.connect(ctx.destination)

    osc.start(now)
    osc.stop(now + 0.1)
    this.vibrate(8)
  }

  // 2. Ascending peaceful two-tone chord chime (F5 -> A5)
  open() {
    if (!this.soundEnabled) {
      this.vibrate(15)
      return
    }
    const ctx = this.getContext()
    const now = ctx.currentTime

    const osc1 = ctx.createOscillator()
    const gain1 = ctx.createGain()
    osc1.type = 'sine'
    osc1.frequency.setValueAtTime(698.46, now)
    gain1.gain.setValueAtTime(0, now)
    gain1.gain.linearRampToValueAtTime(0.28, now + 0.004)
    gain1.gain.exponentialRampToValueAtTime(0.0001, now + 0.18)
    osc1.connect(gain1)
    gain1.connect(ctx.destination)
    osc1.start(now)
    osc1.stop(now + 0.19)

    const osc2 = ctx.createOscillator()
    const gain2 = ctx.createGain()
    osc2.type = 'sine'
    osc2.frequency.setValueAtTime(880.0, now + 0.045)
    gain2.gain.setValueAtTime(0, now + 0.045)
    gain2.gain.linearRampToValueAtTime(0.32, now + 0.05)
    gain2.gain.exponentialRampToValueAtTime(0.0001, now + 0.22)
    osc2.connect(gain2)
    gain2.connect(ctx.destination)
    osc2.start(now + 0.045)
    osc2.stop(now + 0.23)
    this.vibrate(15)
  }

  // 3. Velvet ambient swell
  drawer() {
    if (!this.soundEnabled) {
      this.vibrate(10)
      return
    }
    const ctx = this.getContext()
    const now = ctx.currentTime
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()

    osc.type = 'triangle'
    osc.frequency.setValueAtTime(293.66, now)

    gain.gain.setValueAtTime(0, now)
    gain.gain.linearRampToValueAtTime(0.2, now + 0.08)
    gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.18)

    osc.connect(gain)
    gain.connect(ctx.destination)

    osc.start(now)
    osc.stop(now + 0.2)
    this.vibrate(10)
  }

  // 4. Grounded descending calm tone
  back() {
    if (!this.soundEnabled) {
      this.vibrate(6)
      return
    }
    const ctx = this.getContext()
    const now = ctx.currentTime
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()

    osc.type = 'sine'
    osc.frequency.setValueAtTime(440.0, now)
    osc.frequency.exponentialRampToValueAtTime(329.63, now + 0.14)

    gain.gain.setValueAtTime(0, now)
    gain.gain.linearRampToValueAtTime(0.25, now + 0.004)
    gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.14)

    osc.connect(gain)
    gain.connect(ctx.destination)

    osc.start(now)
    osc.stop(now + 0.15)
    this.vibrate(6)
  }

  // 5. Micro organic tick
  tick() {
    if (!this.soundEnabled) {
      this.vibrate(4)
      return
    }
    const ctx = this.getContext()
    const now = ctx.currentTime
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()

    osc.type = 'sine'
    osc.frequency.setValueAtTime(880.0, now)

    gain.gain.setValueAtTime(0, now)
    gain.gain.linearRampToValueAtTime(0.18, now + 0.002)
    gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.035)

    osc.connect(gain)
    gain.connect(ctx.destination)

    osc.start(now)
    osc.stop(now + 0.04)
    this.vibrate(4)
  }
}

export const ZenAudio = new ZenAudioEngine()
