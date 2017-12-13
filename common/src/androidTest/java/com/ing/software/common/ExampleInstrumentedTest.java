package com.ing.software.common;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.ing.software.common.test", appContext.getPackageName());
    }
//
//    @Test
//    public void fdjskhfjksdhf() {
//        Ransac<Object, Object> fdsf = new Ransac<>(new ArrayList<>(), 1, 2, 3, new Ransac.ModelHandler<Object, Object>() {
//            @Override
//            public double calcModelFromElements(List<Object> eList, Ref<Object> modRef) {
//                return 0;
//            }
//
//            @Override
//            public double inverseError(Object mod, Object elem) {
//                return 0;
//            }
//        });
//    }

    @Test
    public void java8test() {
        //List<Integer> list = new ArrayList<>(Arrays.asList(1,2,3));
        //boolean fds = list.stream().allMatch(integer -> integer < 4);
    }
}
