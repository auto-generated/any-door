package io.github.lgp547.anydoor.core;

import io.github.lgp547.anydoor.dto.AnyDoorDto;
import io.github.lgp547.anydoor.util.AopUtil;
import io.github.lgp547.anydoor.util.BeanUtil;
import io.github.lgp547.anydoor.util.ClassUtil;
import io.github.lgp547.anydoor.util.JsonUtil;
import io.github.lgp547.anydoor.util.SpringUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class AnyDoorService {

    private static final String ANY_DOOR_RUN_MARK = "any-door run ";

    public AnyDoorService() {
    }

    public Object run(AnyDoorDto anyDoorDto) {
        try {
            anyDoorDto.verify();
            Class<?> clazz = Class.forName(anyDoorDto.getClassName());
            Method method = ClassUtil.getMethod(clazz, anyDoorDto.getMethodName(), anyDoorDto.getParameterTypes());

            boolean containsBean = SpringUtil.containsBean(clazz);
            Object bean;
            if (!containsBean) {
                bean = BeanUtil.instantiate(clazz);
            } else {
                bean = SpringUtil.getBean(clazz);
                if (!Modifier.isPublic(method.getModifiers())) {
                    bean = AopUtil.getTargetObject(bean);
                }
            }
            return doRun(anyDoorDto, method, bean);
        } catch (Exception e) {
            System.err.println("anyDoorService run exception. param [" + anyDoorDto + "]");
            e.printStackTrace();
            return null;
        }
    }


    /**
     * {@code  io.github.lgp547.anydoor.attach.AnyDoorAttach#AnyDoorRun(String)}
     */
    public Object run(String anyDoorDtoStr, Method method, Object bean) {
        if (null == anyDoorDtoStr || anyDoorDtoStr.isEmpty()) {
            System.err.println("anyDoorService run exception. anyDoorDtoStr is empty");
            return null;
        }
        if (null == method || null == bean) {
            System.err.println("anyDoorService run exception. method or bean is null");
            return null;
        }

        try {
            return doRun(JsonUtil.toJavaBean(anyDoorDtoStr, AnyDoorDto.class), method, bean);
        } catch (Throwable e) {
            System.err.println("anyDoorService run exception. param [" + anyDoorDtoStr + "]");
            e.printStackTrace();
            return null;
        }
    }

    public Object doRun(AnyDoorDto anyDoorDto, Method method, Object bean) {
        String methodName = method.getName();
        Map<String, Object> contentMap = anyDoorDto.getContentMap();

        AnyDoorHandlerMethod handlerMethod = new AnyDoorHandlerMethod(bean, method);
        Object result = null;
        if (Objects.equals(anyDoorDto.getSync(), true)) {
            if (anyDoorDto.getNum() == 1) {
                result = handlerMethod.invokeSync(contentMap);
            } else {
                if (anyDoorDto.getConcurrent()) {
                    throw new IllegalArgumentException("Concurrent calls do not support sync");
                }
                handlerMethod.parallelInvokeSync(contentMap, anyDoorDto.getNum(), resultLogConsumer(methodName));
            }
        } else {
            if (anyDoorDto.getNum() == 1) {
                handlerMethod.invokeAsync(contentMap).whenComplete(futureResultLogConsumer(methodName));
            } else {
                if (anyDoorDto.getConcurrent()) {
                    handlerMethod.concurrentInvokeAsync(contentMap, anyDoorDto.getNum(), resultLogConsumer(methodName), excLogConsumer(methodName));
                } else {
                    handlerMethod.parallelInvokeAsync(contentMap, anyDoorDto.getNum(), resultLogConsumer(methodName));
                }
            }
        }
        if (result != null) {
            System.out.println(ANY_DOOR_RUN_MARK + methodName + " return: " + JsonUtil.toStrNotExc(result));
        }
        return result;
    }

    private BiConsumer<Integer, Object> resultLogConsumer(String methodName) {
        return (num, result) -> System.out.println(ANY_DOOR_RUN_MARK + methodName + " " + num + " return: " + JsonUtil.toStrNotExc(result));
    }

    private BiConsumer<Integer, Throwable> excLogConsumer(String methodName) {
        return (num, throwable) -> System.out.println(ANY_DOOR_RUN_MARK + methodName + " " + num + " exception: " + throwable.getMessage());
    }

    private BiConsumer<Object, Throwable> futureResultLogConsumer(String methodName) {
        return (result, throwable) -> {
            if (throwable != null) {
                System.out.println(ANY_DOOR_RUN_MARK + methodName + " exception: " + throwable.getMessage());
            } else {
                System.out.println(ANY_DOOR_RUN_MARK + methodName + " return: " + JsonUtil.toStrNotExc(result));
            }
        };
    }
}
