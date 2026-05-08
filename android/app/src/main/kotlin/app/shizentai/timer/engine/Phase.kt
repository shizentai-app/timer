package app.shizentai.timer.engine

enum class Phase {
    IDLE,
    PREPARE,
    WORK,
    WARNING,
    REST,
    FINISHED,
}

enum class AudioCue {
    WORK_START,    // gong_twice
    WORK_WARNING,  // alert
    REST_START,    // gong
    REST_ENDING,   // rest_end (10s before rest ends)
    FINISHED,      // gong
}
