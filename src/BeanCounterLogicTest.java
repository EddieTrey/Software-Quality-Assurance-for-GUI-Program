import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import gov.nasa.jpf.vm.Verify;
import java.util.Random;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Code by @author Wonsun Ahn
 * 
 * <p>Uses the Java Path Finder model checking tool to check BeanCounterLogic in
 * various modes of operation. It checks BeanCounterLogic in both "luck" and
 * "skill" modes for various numbers of slots and beans. It also goes down all
 * the possible random path taken by the beans during operation.
 */

public class BeanCounterLogicTest {
	private static BeanCounterLogic logic; // The core logic of the program
	private static Bean[] beans; // The beans in the machine
	private static String failString; // A descriptive fail string for assertions

	private static int slotCount; // The number of slots in the machine we want to test
	private static int beanCount; // The number of beans in the machine we want to test
	private static boolean isLuck; // Whether the machine we want to test is in "luck" or "skill" mode

	/**
	 * Sets up the test fixture.
	 */
	@BeforeClass
	public static void setUp() {
		if (Config.getTestType() == TestType.JUNIT) {
			slotCount = 5;
			beanCount = 3;
			isLuck = true;
		} else if (Config.getTestType() == TestType.JPF_ON_JUNIT) {
			slotCount = Verify.getInt(1, 5);
			beanCount = Verify.getInt(0, 3);
			isLuck = Verify.getBoolean();
		} else {
			assert (false);
		}

		// Create the internal logic
		logic = BeanCounterLogic.createInstance(slotCount);
		// Create the beans
		beans = new Bean[beanCount];
		for (int i = 0; i < beanCount; i++) {
			beans[i] = Bean.createInstance(slotCount, isLuck, new Random(42));
		}

		// A failstring useful to pass to assertions to get a more descriptive error.
		failString = "Failure in (slotCount=" + slotCount
				+ ", beanCount=" + beanCount + ", isLucky=" + isLuck + "):";
	}

	/**
	 * Tear down.
	 */

	@AfterClass
	public static void tearDown() {
		slotCount = 0;
		beanCount = 0;
		isLuck = false;
		logic = null;
		beans = null;
	}

	/**
	 * Test case for void reset(Bean[] beans).
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 * Invariants: If beanCount is greater than 0,
	 *             remaining bean count is beanCount - 1
	 *             in-flight bean count is 1 (the bean initially at the top)
	 *             in-slot bean count is 0.
	 *             If beanCount is 0,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is 0.
	 */
	@Test
	public void testReset() {
		logic.reset(beans);

		if (beanCount > 0) {
			assertEquals(failString, logic.getRemainingBeanCount(), beanCount - 1);
			assertEquals(failString, logic.getInFlightBeanXPos(0), 0);
			for (int i = slotCount; i < 0; i--) {
				assertEquals(failString, logic.getSlotBeanCount(i), 0);
			}
		}
		if (beanCount == 0) {
			assertEquals(failString, logic.getRemainingBeanCount(), 0);
			assertEquals(failString, logic.getInFlightBeanXPos(0), -1);
			for (int i = slotCount; i < 0; i--) {
				assertEquals(failString, logic.getSlotBeanCount(i), 0);
			}
		}
	}

	/**
	 * Test case for boolean advanceStep().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After each advanceStep(),
	 *             all positions of in-flight beans are legal positions in the logical coordinate system.
	 */
	@Test
	public void testAdvanceStepCoordinates() {
		logic.reset(beans);
		while (logic.advanceStep()) {
			for (int i = 0; i < slotCount; i++) {
				assertTrue(failString, logic.getInFlightBeanXPos(i) < slotCount);
			}
		}
	}

	/**
	 * Test case for boolean advanceStep().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After each advanceStep(),
	 *             the sum of remaining, in-flight, and in-slot beans is equal to beanCount.
	 */
	@Test
	public void testAdvanceStepBeanCount() {
		logic.reset(beans);
		while (logic.advanceStep() != false) {
      int inFlight = 0;
		  int inSlot = 0;

			for (int i = 0; i < slotCount; i++) {
				if (logic.getInFlightBeanXPos(i) != -1) {
					inFlight++;
				}
			
			 //for (int i = 0; i < slotCount; i++)  {
				inSlot += logic.getSlotBeanCount(i);
			 //}
      }
			int total = inSlot + inFlight;
			assertEquals(failString, beanCount, logic.getRemainingBeanCount() + total);
		}
	}

	/**
	 * Test case for boolean advanceStep().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After the machine terminates,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is beanCount.
	 */
	@Test
	public void testAdvanceStepPostCondition() {
		int inFlight = 0;
		int inSlot = 0;
		logic.reset(beans);
		while (logic.advanceStep()) {
		}
		assertEquals(0, logic.getRemainingBeanCount());
		for (int i = 0; i < slotCount; i++) {
			if (logic.getInFlightBeanXPos(i) < 0) {
				continue;
			}
			inFlight++;
		}
		assertEquals(0, inFlight);
		for (int i = 0; i < slotCount; i++) {
			inSlot += logic.getSlotBeanCount(i);
		}
		assertEquals(beanCount, inSlot);
	}
	
	/**
	 * Test case for void lowerHalf()().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.lowerHalf().
	 * Invariants: After the machine terminates,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is beanCount.
	 *             After calling logic.lowerHalf(),
	 *             slots in the machine contain only the lower half of the original beans.
	 *             Remember, if there were an odd number of beans, (N+1)/2 beans should remain.
	 *             Check each slot for the expected number of beans after having called logic.lowerHalf().
	 */
	@Test
	public void testLowerHalf() {
		logic.reset(beans);
		while (logic.advanceStep()) {
		}
		logic.lowerHalf();
		int bottom = 0;
		for (int i = 0; i < slotCount; i++) {
			bottom += logic.getSlotBeanCount(i);
		}
		if (beanCount % 2 == 0) {
			assertEquals(failString, beanCount / 2, bottom);
		} else {
			assertEquals(failString, (beanCount + 1) / 2, bottom);
		}
	}
	
	/**
	 * Test case for void upperHalf().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.upperHalf().
	 * Invariants: After the machine terminates,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is beanCount.
	 *             After calling logic.upperHalf(),
	 *             slots in the machine contain only the upper half of the original beans.
	 *             Remember, if there were an odd number of beans, (N+1)/2 beans should remain.
	 *             Check each slot for the expected number of beans after having called logic.upperHalf().
	 */
	@Test
	public void testUpperHalf() {
		logic.reset(beans);
		while (logic.advanceStep()) {
		}
		logic.upperHalf();
		int lower = 0;
		for (int i = 0; i < slotCount; i++) {
			lower += logic.getSlotBeanCount(i);
		}
		if (beanCount % 2 == 0) {
			assertEquals(failString, beanCount / 2, lower);
		} else {
			assertEquals(failString, (beanCount + 1) / 2, lower);
		}
	}
	
	/**
	 * Test case for void repeat().
	 * Preconditions: The machine is operating in skill mode.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.repeat();
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: Bean count in each slot is identical after the first run and second run of the machine. 
	 */
	@Test
	public void testRepeat() {
		if (!isLuck) {
			int[] run1 = new int[slotCount];
			int[] run2 = new int[slotCount];
			logic.reset(beans);
			while (logic.advanceStep()) {
			}
			for (int i = 0; i < slotCount; i++) {
				run1[i] = logic.getSlotBeanCount(i);
			}
			logic.repeat();

			while (logic.advanceStep()) {
			}
			for (int i = 0; i < slotCount; i++) {
				run2[i] = logic.getSlotBeanCount(i);
			}

			for (int i = 0; i < slotCount; i++) {
				assertEquals(failString, run1[i], run2[i]);
			}
		}
	}

  /**
	 * Test case for getAverageSlotBeanCount().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.getAverageSlotBeanCount();
	 * Invariants: After the machine terminates,
	 * 			   getAverageSlotBeanCount() returns average slot number of beans in slots
	 * 			   average of bean slots *  beancount = the average slot number of all beans in slots.
	 */
  @Test
	public void testAverageSlotBeanCount() {
		logic.reset(beans);
    double howManyBeans = 0;
    double avgBeans = 0;
  //gets beanzzzzzzzz * slots
		while (logic.advanceStep()) {

    }
		for (int i = 0; i < slotCount; i++) {
			howManyBeans += (i * logic.getSlotBeanCount(i));
    }
		
  //avg slot number * beanzzzzz
		avgBeans = logic.getAverageSlotBeanCount() * beanCount;

		assertEquals(failString, howManyBeans, avgBeans, 0.0);
	}
}
