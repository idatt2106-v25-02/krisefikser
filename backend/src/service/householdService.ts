// ...existing code...

async deleteHousehold(householdId: number): Promise<void> {
  // Create a query runner for transaction
  const queryRunner = AppDataSource.createQueryRunner();
  await queryRunner.connect();
  await queryRunner.startTransaction();
  
  try {
    // Update all users who have this household as their active household
    await queryRunner.manager
      .createQueryBuilder()
      .update(User)
      .set({ activeHousehold: null })
      .where("active_household_id = :householdId", { householdId })
      .execute();
    
    // Now delete the household
    const household = await queryRunner.manager.findOneBy(Household, { id: householdId });
    
    if (!household) {
      throw new Error("Household not found");
    }
    
    await queryRunner.manager.remove(household);
    
    // Commit the transaction
    await queryRunner.commitTransaction();
  } catch (error) {
    // If error, rollback the transaction
    await queryRunner.rollbackTransaction();
    throw error;
  } finally {
    // Release the query runner
    await queryRunner.release();
  }
}

// ...existing code...

