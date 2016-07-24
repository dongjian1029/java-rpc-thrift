package main;

import java.util.LinkedList;
import java.util.List;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.appchina.rpc.test.api.AddService;
import com.appchina.rpc.test.api.AddService.AddServiceException;
import com.appchina.rpc.test.api.MoodService;
import com.appchina.rpc.test.api.model.GENDER;
import com.appchina.rpc.test.api.model.Mood;

public class Client {

	public static void main(String[] args) throws Exception {
		final ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("/application-client.xml");
		
		testError((AddService) ctx.getBean("addService"));
		testError((AddService) ctx.getBean("otherAddService"));
		testMood((MoodService) ctx.getBean("moodService"));
		testPerf((AddService) ctx.getBean("addService"));
		
//		final AddService service = (AddService) ctx.getBean("addService");
//		for(int i=0; i<50; i++){
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						testPerf(service);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}).start();
//		}
//		Thread.sleep(10000000);

		ctx.close();
	}

	public static void testPerf(AddService addService) throws Exception {
		long start = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			addService.add(i);
		}
		System.out.println("times:" + (System.currentTimeMillis() - start));
	}

	public static void testError(AddService addService) throws Exception {
		try {
			addService.exception();
			System.err.println("no error.");
		} catch (AddServiceException e) {
			e.printStackTrace();
		}
	}

	public static void testMood(MoodService moodService) throws Exception {
		List<Mood> moodList = new LinkedList<Mood>();
		moodList.add(new Mood());
		moodList.add(new Mood());
		moodList.add(new Mood());
		moodList.add(new Mood());
		moodList.add(new Mood());
		moodService.test();
		System.out.println(moodService.test(1));
		System.out.println(moodService.test("name"));
		System.out.println(moodService.test(1, "name"));
		System.out.println(moodService.test(new Mood()));
		System.out.println(moodService.test(moodList));
		System.out.println(moodService.test(new Mood[] { new Mood() }));
		System.out.println(moodService.test(new int[] { 1, 2, 3 }));
		System.out.println(moodService.test(0, 1));
		System.out.println(moodService.test(GENDER.W));
	}

}
