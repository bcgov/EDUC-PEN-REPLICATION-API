package ca.bc.gov.educ.api.pen.replication;

import ca.bc.gov.educ.api.pen.replication.support.TestRedisConfiguration;
import ca.bc.gov.educ.api.pen.replication.utils.PenReplicationTestUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.when;

/**
 * The type Base pen replication api test.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestRedisConfiguration.class, PenReplicationApiResourceApplication.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class BasePenReplicationAPITest {

  @MockBean
  private ValueOperations valueOperations;
  @MockBean
  private StringRedisTemplate stringRedisTemplate;

  /**
   * The Pen replication test utils.
   */
  @Autowired
  protected PenReplicationTestUtils penReplicationTestUtils;

  /**
   * Reset state.
   */
  @Before
  public void resetState() {
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    this.penReplicationTestUtils.cleanDB();
  }
}
