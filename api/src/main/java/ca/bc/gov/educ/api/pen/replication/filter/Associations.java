package ca.bc.gov.educ.api.pen.replication.filter;

import jakarta.persistence.criteria.Join;
import lombok.Getter;

import java.util.*;

/**
 * The type Associations.
 */
public class Associations {
  /**
   * The association names in the orderBy statement
   */
  @Getter
  private final Set<String> sortAssociations = new HashSet<>();

  /**
   * The association names in the where condition
   */
  @Getter
  private final List<String> searchAssociations = new ArrayList<>();
  /**
   * The joined association names in the where condition
   */
  @Getter
  private final Map<String, Join<Object, Object>> joinedSearchAssociations = new HashMap<>();
  /**
   * The count for joining association
   */
  @Getter
  private int joinedCount = 0;

  /**
   * Count the join operation and return the cached join operation
   * also reset the count and remove the cache when all associations are joined because Hibernate may run another count query to determine the number of results
   *
   * @param associationName the association name
   * @return join the cached join operation
   */
  public Join<Object, Object> countJoin(final String associationName) {
    final var join = this.joinedSearchAssociations.get(associationName);
    this.joinedCount++;
    return join;
  }

  /**
   * Cache the join operation because we just need one join operation for one association
   *
   * @param associationName the association name
   * @param join            the join operation
   * @return the count for the join operation
   */
  public int cacheJoin(final String associationName, final Join<Object, Object> join) {
    this.joinedSearchAssociations.put(associationName, join);
    return this.joinedCount;
  }

  /**
   * reset the count and remove the cache if all joins are processed
   */
  public void resetIfAllJoinsProcessed() {
    if (this.searchAssociations.size() == this.joinedCount) {
      this.reset();
    }
  }

  /**
   * reset the count and remove the cache
   */
  public void reset() {
    this.joinedCount = 0;
    this.joinedSearchAssociations.clear();
  }

}
