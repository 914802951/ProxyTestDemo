package com.lz.proxytestdemo.sdlapp;

import com.smartdevicelink.proxy.RPCRequest;
import com.smartdevicelink.proxy.rpc.enums.UpdateMode;

import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Created by Administrator on 2017/11/23.
 */
public class MediaSdlAppTest {

    @Test
    public void setHmiMediaClockTimer(){
        Method method = null;
        try {
            MediaSdlApp app = Mockito.mock(MediaSdlApp.class);
            Class[] parameterTypes = {Integer.class, Integer.class, UpdateMode.class};
            method = MediaSdlApp.class.getDeclaredMethod("setHmiMediaClockTimer", parameterTypes);
            method.setAccessible(true);

            Object[] arguments = new Object[]{null, null, UpdateMode.COUNTUP};
            method.invoke(app, arguments);
            verify(app, never()).sendRpcMsg(any(RPCRequest.class));

            arguments = new Object[]{0, null, UpdateMode.COUNTUP};
            method.invoke(app, arguments);
            verify(app).sendRpcMsg(any(RPCRequest.class));
        }catch (Exception e){
            e.printStackTrace();
            fail();
        }finally {
            if (method != null) {
                method.setAccessible(false);
            }
        }
    }

    @Test
    public void checkMediaClockTimer() {
        int max = 59 * 60 * 60;
        Method method = null;
        try {
            MediaSdlApp app = Mockito.mock(MediaSdlApp.class);
            Class[] parameterTypes = {Integer.class, Integer.class, UpdateMode.class};
            method = MediaSdlApp.class.getDeclaredMethod("checkMediaClockTimer", parameterTypes);
            method.setAccessible(true);

            Object[] arguments = new Object[]{null, null, UpdateMode.COUNTUP};
            boolean result = (boolean) method.invoke(app, arguments);
            assertEquals(false, result);

            arguments = new Object[]{-1, null, UpdateMode.COUNTUP};
            result = (boolean) method.invoke(app, arguments);
            assertEquals(false, result);

            arguments = new Object[]{max + 1, null, UpdateMode.COUNTUP};
            result = (boolean) method.invoke(app, arguments);
            assertEquals(false, result);

            arguments = new Object[]{0, null, UpdateMode.COUNTUP};
            result = (boolean) method.invoke(app, arguments);
            assertEquals(true, result);

            arguments = new Object[]{0, 10, UpdateMode.COUNTUP};
            result = (boolean) method.invoke(app, arguments);
            assertEquals(true, result);

            arguments = new Object[]{20, 10, UpdateMode.COUNTUP};
            result = (boolean) method.invoke(app, arguments);
            assertEquals(false, result);

            arguments = new Object[]{20, 10, UpdateMode.COUNTDOWN};
            result = (boolean) method.invoke(app, arguments);
            assertEquals(true, result);

            arguments = new Object[]{null, 10, UpdateMode.PAUSE};
            result = (boolean) method.invoke(app, arguments);
            assertEquals(true, result);
        }catch (Exception e){
            e.printStackTrace();
            fail();
        }finally {
            if (method != null) {
                method.setAccessible(false);
            }
        }
    }
}