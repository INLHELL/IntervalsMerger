package main;

import org.springframework.context.support.GenericXmlApplicationContext;
import utils.IntervalsGenerator;
import utils.IntervalsMerger;

public class Main {
    public static void main(String[] args) {
        final GenericXmlApplicationContext context = new GenericXmlApplicationContext();
        context.load("classpath:spring-context.xml");
        context.refresh();
        context.getBean(IntervalsGenerator.class);
        final IntervalsMerger merger = context.getBean(IntervalsMerger.class);
        merger.executeWorkers();

    }
}
