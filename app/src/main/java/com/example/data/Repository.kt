package com.example.data

import kotlinx.coroutines.flow.Flow

class StrategyRepository(
    private val strategyDao: StrategyDao,
    private val matchDao: MatchDao,
    private val noteDao: NoteDao
) {
    // Strategies
    val allStrategies: Flow<List<StrategyEntity>> = strategyDao.getAllStrategies()
    
    suspend fun getStrategyById(id: Int): StrategyEntity? = strategyDao.getStrategyById(id)
    
    suspend fun insertStrategy(strategy: StrategyEntity): Long = strategyDao.insertStrategy(strategy)
    
    suspend fun deleteStrategyById(id: Int) = strategyDao.deleteStrategyById(id)
    
    // Matches
    val allMatches: Flow<List<MatchEntity>> = matchDao.getAllMatches()
    
    suspend fun insertMatch(match: MatchEntity): Long = matchDao.insertMatch(match)
    
    suspend fun deleteMatchById(id: Int) = matchDao.deleteMatchById(id)
    
    // Notes
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    
    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes("%$query%")
    
    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)
    
    suspend fun deleteNoteById(id: Int) = noteDao.deleteNoteById(id)
}
