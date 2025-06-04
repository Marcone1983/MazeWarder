package com.marcone1983.mazewarden3d

object SkillSystem {

    fun useSkill(character: String, mazeView: MazeView) {
        when (character) {
            "Guerriero" -> mazeView.destroyRandomWall()
            "Maga" -> mazeView.teleportRandom()
            "Robot" -> mazeView.scanAndRevealExit()
        }
    }
}