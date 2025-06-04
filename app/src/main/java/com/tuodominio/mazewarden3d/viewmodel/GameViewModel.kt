package com.marcone1983.mazewarden3d.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.marcone1983.mazewarden3d.model.*
import com.marcone1983.mazewarden3d.repository.GameRepository

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState.initial())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _mazeState = MutableStateFlow(MazeState.empty())
    val mazeState: StateFlow<MazeState> = _mazeState.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerState.initial())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _audioState = MutableStateFlow(AudioState.initial())
    val audioState: StateFlow<AudioState> = _audioState.asStateFlow()

    init {
        initializeGame()
    }

    private fun initializeGame() {
        viewModelScope.launch {
            try {
                _gameState.value = _gameState.value.copy(isLoading = true)
                
                val savedState = gameRepository.loadGameState()
                if (savedState != null) {
                    _gameState.value = savedState
                    _mazeState.value = gameRepository.loadMazeState() ?: MazeState.empty()
                    _playerState.value = gameRepository.loadPlayerState() ?: PlayerState.initial()
                } else {
                    startNewGame()
                }
                
                _gameState.value = _gameState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _gameState.value = _gameState.value.copy(
                    isLoading = false,
                    error = "Errore durante l'inizializzazione: ${e.message}"
                )
            }
        }
    }

    fun startNewGame() {
        viewModelScope.launch {
            _gameState.value = GameState.initial()
            _mazeState.value = MazeState.generate(size = 11)
            _playerState.value = PlayerState.initial()
            _audioState.value = AudioState.initial()
            
            saveGameState()
        }
    }

    fun movePlayer(direction: Direction) {
        viewModelScope.launch {
            val currentPlayer = _playerState.value
            val currentMaze = _mazeState.value
            
            val newPosition = calculateNewPosition(currentPlayer.position, direction)
            
            if (isValidMove(newPosition, currentMaze)) {
                _playerState.value = currentPlayer.copy(
                    position = newPosition,
                    moveCount = currentPlayer.moveCount + 1
                )
                
                _audioState.value = _audioState.value.copy(
                    lastSound = SoundEffect.STEP
                )
                
                checkVictoryCondition()
                triggerAIMove()
            } else {
                _audioState.value = _audioState.value.copy(
                    lastSound = SoundEffect.WALL_HIT
                )
            }
            
            saveGameState()
        }
    }

    fun useSkill(skill: Skill) {
        viewModelScope.launch {
            val currentPlayer = _playerState.value
            
            if (canUseSkill(currentPlayer, skill)) {
                when (skill) {
                    Skill.WALL_DESTROYER -> {
                        _mazeState.value = _mazeState.value.destroyRandomWall()
                        _audioState.value = _audioState.value.copy(lastSound = SoundEffect.SKILL_USE)
                    }
                    Skill.TELEPORT -> {
                        val newPos = _mazeState.value.getRandomEmptyPosition()
                        _playerState.value = currentPlayer.copy(position = newPos)
                        _audioState.value = _audioState.value.copy(lastSound = SoundEffect.SKILL_USE)
                    }
                    Skill.EXIT_SCANNER -> {
                        _gameState.value = _gameState.value.copy(isExitRevealed = true)
                        _audioState.value = _audioState.value.copy(lastSound = SoundEffect.SKILL_USE)
                    }
                }
                
                _playerState.value = _playerState.value.copy(
                    skillCooldowns = _playerState.value.skillCooldowns.toMutableMap().apply {
                        put(skill, skill.cooldownTurns)
                    }
                )
                
                saveGameState()
            }
        }
    }

    fun selectCharacter(character: Character) {
        viewModelScope.launch {
            _playerState.value = _playerState.value.copy(character = character)
            saveGameState()
        }
    }

    fun updateAudioSettings(settings: AudioSettings) {
        viewModelScope.launch {
            _audioState.value = _audioState.value.copy(settings = settings)
            gameRepository.saveAudioSettings(settings)
        }
    }

    private fun triggerAIMove() {
        viewModelScope.launch {
            val aiMove = gameRepository.calculateAIMove(_mazeState.value, _playerState.value)
            if (aiMove != null) {
                _mazeState.value = _mazeState.value.applyAIMove(aiMove)
            }
        }
    }

    private fun checkVictoryCondition() {
        val currentPlayer = _playerState.value
        val currentMaze = _mazeState.value
        
        if (currentPlayer.position == currentMaze.exitPosition) {
            _gameState.value = _gameState.value.copy(
                isGameWon = true,
                finalScore = calculateScore(currentPlayer, _gameState.value)
            )
            _audioState.value = _audioState.value.copy(lastSound = SoundEffect.VICTORY)
        }
    }

    private fun calculateNewPosition(current: Position, direction: Direction): Position {
        return when (direction) {
            Direction.UP -> current.copy(y = current.y - 1)
            Direction.DOWN -> current.copy(y = current.y + 1)
            Direction.LEFT -> current.copy(x = current.x - 1)
            Direction.RIGHT -> current.copy(x = current.x + 1)
        }
    }

    private fun isValidMove(position: Position, maze: MazeState): Boolean {
        return maze.isPositionValid(position) && !maze.hasWallAt(position)
    }

    private fun canUseSkill(player: PlayerState, skill: Skill): Boolean {
        return (player.skillCooldowns[skill] ?: 0) <= 0
    }

    private fun calculateScore(player: PlayerState, game: GameState): Int {
        val baseScore = 1000
        val movesPenalty = player.moveCount * 10
        val timeBonusMs = maxOf(0, 300_000 - game.elapsedTimeMs) // 5 min bonus
        return baseScore - movesPenalty + (timeBonusMs / 1000)
    }

    private fun saveGameState() {
        viewModelScope.launch {
            gameRepository.saveGameState(_gameState.value)
            gameRepository.saveMazeState(_mazeState.value)
            gameRepository.savePlayerState(_playerState.value)
        }
    }

    fun pauseGame() {
        _gameState.value = _gameState.value.copy(isPaused = true)
    }

    fun resumeGame() {
        _gameState.value = _gameState.value.copy(isPaused = false)
    }

    fun resetGame() {
        startNewGame()
    }

    override fun onCleared() {
        super.onCleared()
        // ViewModel cleanup automatically handled by viewModelScope
    }
}