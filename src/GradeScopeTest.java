import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GradeScopeTest {

	private final int[] beanCounts = { 0, 2, 20, 200 };
	private final int[] logicSlotCounts = { 1, 10, 20 };
	private BeanCounterLogic[] logics;
	private Random rand;
	
	ByteArrayOutputStream out;
	PrintStream stdout;

	private String getFailString(int logicIndex, int beanCount) {
		return "[Slot Count = " + logicSlotCounts[logicIndex] + "] Test case with " + beanCount
				+ " initial beans failed";
	}

	private Bean[] createBeans(int slotCount, int beanCount, boolean luck) {
		Bean[] beans = new Bean[beanCount];
		for (int i = 0; i < beanCount; i++) {
			beans[i] = Bean.createInstance(slotCount, luck, rand);
		}
		return beans;
	}

	private int getInFlightBeanCount(BeanCounterLogic logic, int slotCount) {
		int inFlight = 0;
		for (int yPos = 0; yPos < slotCount; yPos++) {
			int xPos = logic.getInFlightBeanXPos(yPos);
			if (xPos != BeanCounterLogic.NO_BEAN_IN_YPOS) {
				inFlight++;
			}
		}
		return inFlight;
	}

	private int getInSlotsBeanCount(BeanCounterLogic logic, int slotCount) {
		int inSlots = 0;
		for (int i = 0; i < slotCount; i++) {
			inSlots += logic.getSlotBeanCount(i);
		}
		return inSlots;
	}

	/**
	 * The test fixture. Creates multiple machines (logics) with different slot
	 * counts. It also creates a real random object. But the random object is seeded
	 * with 42 so the tests will be reproducible.
	 */
	@Before
	public void setUp() {
		logics = new BeanCounterLogic[logicSlotCounts.length];
		for (int i = 0; i < logics.length; i++) {
			logics[i] = BeanCounterLogic.createInstance(logicSlotCounts[i]);
		}
		rand = new Random(42);
		
		out = new ByteArrayOutputStream();
		stdout = System.out;
		try {
			System.setOut(new PrintStream(out, false, Charset.defaultCharset().toString()));
		} catch (UnsupportedEncodingException uex) {
			fail();
		}
	}

	@After
	public void tearDown() {
		logics = null;
		rand = null;
		out = null;
		
		System.setOut(stdout);
	}

	/**
	 * Test reset(Bean[]). 
	 * Preconditions: logics for each slot count in logicSlotCounts are created.
	 * Execution steps: For each logic, and for each bean count in beanCounts,
	 *                  Call createBeans to create lucky beans for the slot count and bean count
	 *                  Call logic.reset(beans). 
	 * Invariants: If initial bean count is greater than 0,
	 *             remaining bean count is beanCount - 1
	 *             in-flight bean count is 1 (the bean initially at the top)
	 *             in-slot bean count is 0.
	 *             If initial bean count is 0,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is 0.
	 */
	@Test
	public void testReset() {
		for (int i = 0; i < logics.length; i++) {
			for (int beanCount : beanCounts) {
				String failString = getFailString(i, beanCount);
				Bean[] beans = createBeans(logicSlotCounts[i], beanCount, true);
				logics[i].reset(beans);

				int remainingObserved = logics[i].getRemainingBeanCount();
				int inFlightObserved = getInFlightBeanCount(logics[i], logicSlotCounts[i]);
				int inSlotsObserved = getInSlotsBeanCount(logics[i], logicSlotCounts[i]);
				int remainingExpected = (beanCount > 0) ? beanCount - 1 : 0;
				int inFlightExpected = (beanCount > 0) ? 1 : 0;
				int inSlotsExpected = 0;
				
				assertEquals(failString + ". Check on remaining bean count",
						remainingExpected, remainingObserved);
				assertEquals(failString + ". Check on in-flight bean count",
						inFlightExpected, inFlightObserved);
				assertEquals(failString + ". Check on in-slot bean count",
						inSlotsExpected, inSlotsObserved);
			}
		}
	}

	/**
	 * Test advanceStep() in luck mode.
	 * Preconditions: logics for each slot count in logicSlotCounts are created.
	 * Execution steps: For each logic, and for each bean count in beanCounts,
	 *                  Call createBeans to create lucky beans for the slot count and bean count
	 *                  Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After each advanceStep(),
	 *             1) The remaining bean count, 2) the in-flight bean count, and 3) the in-slot bean count
	 *             all reflect the correct values at that step of the simulation. 
	 */
	@Test
	public void testAdvanceStepLuckMode() {
		// TODO: Implement
        for (int i = 0; i < logics.length; i++) 
        {
            for (int beanCount : beanCounts) 
            {
				String failString = getFailString(i, beanCount);
				Bean[] beans = createBeans(logicSlotCounts[i], beanCount, true);
				logics[i].reset(beans);
                
				int remainingObserved = 0;
				int inFlightObserved = 0;
				int inSlotsObserved = 0;
				int remainingExpected = (beanCount > 0) ? beanCount - 1 : 0;
				int inFlightExpected = 0;
				int inSlotsExpected = 0;
                int counter = 0;

                while (logics[i].advanceStep())
                {
                    remainingObserved = logics[i].getRemainingBeanCount();
				    inFlightObserved = getInFlightBeanCount(logics[i], logicSlotCounts[i]);
				    inSlotsObserved = getInSlotsBeanCount(logics[i], logicSlotCounts[i]);
                    //bean counts inflight babalbalbala
                    if (counter >= logicSlotCounts[i])
                        inSlotsExpected++;
                    if (remainingExpected > 0)
                        remainingExpected--;
                    inFlightExpected = counter - remainingExpected - inSlotsExpected; //beancount?
                    

                    assertEquals(failString + ". Check on remaining bean count",
							remainingExpected, remainingObserved);
					assertEquals(failString + ". Check on in-flight bean count",
							inFlightExpected, inFlightObserved);
					assertEquals(failString + ". Check on in-slot bean count",
							inSlotsExpected, inSlotsObserved);
					counter++;
                }
            }
        }
	}

	/**
	 * Test advanceStep() in skill mode.
	 * Preconditions: logics for each slot count in logicSlotCounts are created.
	 *                rand.nextGaussian() always returns 0 (to fix skill level to 5).
	 * Execution steps: For the logic with 10 slot counts,
	 *                  Call createBeans to create 200 skilled beans
	 *                  Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After the machine terminates,
	 *             logics.getSlotBeanCount(5) returns 200,
	 *             logics.getSlotBeanCount(i) returns 0, where i != 5. 
	 */
	@Test
	public void testAdvanceStepSkillMode() {
        // changed back to rand and not local Random rand, because we want to use the private one in the class
        rand = Mockito.mock(Random.class);
        Mockito.when(rand.nextGaussian()).thenReturn((double)0);
        String failString = getFailString(1, beanCounts[3]);
		Bean[] beans = createBeans(logicSlotCounts[1], beanCounts[3], false);
		logics[1].reset(beans);
		while (logics[1].advanceStep()); //Call logic.advanceStep() in a loop until it returns false

		for (int i = 0; i < 10; i++)
        {
			if (i != 5)
				assertEquals(failString, 0, logics[1].getSlotBeanCount(i));
			else
			    assertEquals(failString, 200, logics[1].getSlotBeanCount(i));
		}
	}

	/**
	 * Test lowerHalf() in luck mode.
	 * Preconditions: logics for each slot count in logicSlotCounts are created.
	 * Execution steps: For the logic with 10 slot counts, and for each bean count in beanCounts,
	 *                  Call createBeans to create lucky beans for the slot count and bean count
	 *                  Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Construct an expectedSlotCounts array that stores expected bean counts for each slot,
	 *                  after having called logic.lowerHalf().
	 *                  Call logic.lowerHalf().
	 *                  Construct an observedSlotCounts array that stores current bean counts for each slot.
	 * Invariants: expectedSlotCounts matches observedSlotCounts exactly. 
	 */
	@Test
	public void testLowerHalf() {
		/* Test Reset: 
        for (int i = 0; i < logics.length; i++) {
			for (int beanCount : beanCounts) {
				String failString = getFailString(i, beanCount);
				Bean[] beans = createBeans(logicSlotCounts[i], beanCount, true);
				logics[i].reset(beans);
        */
        //10 slots is logics[1].....[1, 10, 20].....and setup has:
        //ogics = new BeanCounterLogic[logicSlotCounts.length];
		for (int beanCount : beanCounts) {
			String failString = getFailString(1, beanCount);
			//lucky beans...man i hate beans now
			Bean[] beans = createBeans(logicSlotCounts[1], beanCount, true);
			logics[1].reset(beans);
			while (logics[1].advanceStep()) {
			}
			;
			//after having called logic.lowerHalf().
			logics[1].lowerHalf();
			int[] expectedSlotCounts = new int[logicSlotCounts[1]];
			//array for expected beans in each slot
			for (int i = 0; i < logicSlotCounts[1]; i++)
				expectedSlotCounts[i] = logics[1].getSlotBeanCount(i);
			logics[1].lowerHalf();

			//assert observed = expected
			int[] observedSlotCounts = new int[logicSlotCounts[1]];
			for (int i = 0; i < logicSlotCounts[1]; i++)
				observedSlotCounts[1] = logics[1].getSlotBeanCount(i);


			assertEquals(expectedSlotCounts, observedSlotCounts);
		}
	}

	/**
	 * Test upperHalf() in luck mode.
	 * Preconditions: logics for each slot count in logicSlotCounts are created.
	 * Execution steps: For the logic with 10 slot counts, and for each bean count in beanCounts,
	 *                  Call createBeans to create lucky beans for the slot count and bean count
	 *                  Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Construct an expectedSlotCounts array that stores expected bean counts for each slot,
	 *                  after having called logic.upperHalf().
	 *                  Call logic.upperHalf().
	 *                  Construct an observedSlotCounts array that stores current bean counts for each slot.
	 * Invariants: expectedSlotCounts matches observedSlotCounts exactly. 
	 */
	@Test
	public void testUpperHalf() {
		// TODO: Implement
        /* Test Reset: 
        for (int i = 0; i < logics.length; i++) {
			for (int beanCount : beanCounts) {
				String failString = getFailString(i, beanCount);
				Bean[] beans = createBeans(logicSlotCounts[i], beanCount, true);
				logics[i].reset(beans);
        */
        //10 slots is logics[1].....[1, 10, 20].....and setup has:
        //ogics = new BeanCounterLogic[logicSlotCounts.length];
		for (int beanCount : beanCounts) {
			String failString = getFailString(1, beanCount);
            //yay BEANSSSSSSSSSSSSSSSSSSSSSSS
			Bean[] beans = createBeans(logicSlotCounts[1], beanCount, true);
			logics[1].reset(beans);
            while(logics[1].advanceStep()){};
            //after having called logic.upperHalf().
            logics[1].upperHalf();
            int[] expectedSlotCounts = new int [logicSlotCounts[1]];
            //array for expected beans in each slot
            for (int i = 0; i < logicSlotCounts[1]; i++)
                expectedSlotCounts[i] = logics[1].getSlotBeanCount(i);
            logics[1].upperHalf();

            //assert observed = expected
            int[] observedSlotCounts = new int[logicSlotCounts[1]];
            for (int i = 0; i < logicSlotCounts[1]; i++)
                observedSlotCounts[1] = logics[1].getSlotBeanCount(i);
            
            
            assertEquals(expectedSlotCounts, observedSlotCounts);
		}
	}

	/**
	 * Test repeat() in skill mode.
	 * Preconditions: logics for each slot count in logicSlotCounts are created.
	 * Execution steps: For the logic with 10 slot counts, and for each bean count in beanCounts,
	 *                  Call createBeans to create skilled beans for the slot count and bean count
	 *                  Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Construct an expectedSlotCounts array that stores current bean counts for each slot.
	 *                  Call logic.repeat().
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Construct an observedSlotCounts array that stores current bean counts for each slot.
	 * Invariants: expectedSlotCounts matches observedSlotCounts exactly. 
	 */
	@Test
	public void testRepeat() {
		for (int i = 0; i < logics.length; i++) {
			for (int beanCount : beanCounts) {
				String failString = getFailString(i, beanCount);
				Bean[] beans = createBeans(logicSlotCounts[i], beanCount, true);
				if (logicSlotCounts[i] == 10) {
					logics[i].reset(beans);
				}
				while (logics[i].advanceStep()) {
				}
				int[] expectedSlotCounts = new int[10];
				for (int j = 0; j < 10; j++) {
					expectedSlotCounts[i] = logics[i].getSlotBeanCount(j);
				}
				logics[i].repeat();
				int[] oberservedSlotCounts = new int[10];
				for (int j = 0; j < 10; j++) {
					oberservedSlotCounts[i] = logics[i].getSlotBeanCount(j);
				}
				for (int j = 0; j < 10; j++) {
					assertEquals(failString, oberservedSlotCounts[j], expectedSlotCounts[j]);
				}
			}
		}
	}

	/**
	 * Test getAverageSlotBeanCount() in luck mode.
	 * Preconditions: logics for each slot count in logicSlotCounts are created.
	 * Execution steps: For the logic with 10 slot counts,
	 *                  Call createBeans to create 200 lucky beans
	 *                  Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Store an expectedAverage, an average of the slot number for each bean.
	 *                  Store an observedAverage, the result of logic.getAverageSlotBeanCount().
	 *                  Store an idealAverage, which is 4.5.
	 * Invariants: Math.abs(expectedAverage - observedAverage) < 0.01.
	 *             Math.abs(idealAverage - observedAverage) < 0.5.
	 */
	@Test
	public void testGetAverageSlotBeanCount() {
		for (int i = 0; i < logics.length; i++) {
			for (int beanCount : beanCounts) {
				String failString = getFailString(i, beanCount);
				Bean[] beans = createBeans(logicSlotCounts[i], beanCount, true);
				if (logicSlotCounts[i] == 10) {
					logics[i].reset(beans);
				}
				while (logics[i].advanceStep()) {
				}
				double expectedAvg = 4.5;
				double observedAvg = logics[i].getAverageSlotBeanCount();
				double idealAverage = 4.5;
				assertTrue(failString, (expectedAvg - observedAvg) < .01);
				assertTrue(failString, (idealAverage - observedAvg) < .5);
			}
		}
	}

	/**
	 * Test main(String[] args).
	 * Preconditions: None.
	 * Execution steps: Check invariants after either calling
	 *                  BeanCounterLogicImpl.main("10", "500", "luck"), or
	 *                  BeanCounterLogicImpl.main("10", "500", "skill").
	 * Invariants: There are two lines of output.
	 *             There are 10 slot counts on the second line of output.
	 *             The sum of the 10 slot counts is equal to 500.
	 */
	@Test
	public void testMain() {
		// TODO: Implement using out.toString() to get output stream
        ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream printOut = System.out;
		BeanCounterLogicImpl.main(new String[]{"10", "500", "luck"});
        String temp0;
        String[] temp1;
        String[] temp2;
        int total = 0;
        
		temp0 = out.toString();
		temp1 = temp0.split("\n");
        //2 lines of output
		assertEquals(temp1.length, 2);

		temp2 = temp1[1].trim().split("\\s+");
        //10 slots for 2nd line
		assertEquals(temp2.length, 10);

		total = 0;
		for (String str : temp2) {
			total = total + Integer.parseInt(str);
		}
        //temp[].indexes added up = 500
		assertEquals(total, 500);

		System.setOut(printOut);
	}

}
