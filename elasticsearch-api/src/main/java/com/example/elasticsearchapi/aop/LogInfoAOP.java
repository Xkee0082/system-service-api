package com.example.elasticsearchapi.aop;

import com.alibaba.fastjson.JSON;
import com.example.elasticsearchapi.constant.ResultCodeEnum;
import com.example.elasticsearchapi.controller.ContentSearchController;
import com.example.elasticsearchapi.entity.ResultDTO;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @Author: xu_ke
 * @Date: 2022-06-26 21:21
 * @description TODO
 */
@Component
@Aspect
@Slf4j
public class LogInfoAOP {

    /**
     * Controller中的所有方法
     */
    @Pointcut("execution(* com.example.elasticsearchapi.controller.ContentSearchController.*(..))")
    public void pointCut() {}

    @Around("pointCut()")
    public Object handleServiceMethod(ProceedingJoinPoint pjp) throws Throwable {

        // 方法执行返回的结果对象
        Object methodResultObj;
        ResultDTO resultDTO = null;

        // 方法是否执行成功标记
        boolean isMethodExeSuccess = true;

        try {
            // 获取当前对象
            Object obj = pjp.getThis();
            // 判断类型
            if (!(obj instanceof ContentSearchController)) {
                return pjp.proceed(pjp.getArgs());
            }
            // 获取切面的Controller对象
            ContentSearchController contentSearchController = (ContentSearchController) pjp.getThis();
            // 构造日志记录内容，包括调用的方法名，参数等内容
            StringBuffer logMsg = new StringBuffer();
            // 获取请求中的属性
            String url = null;
            String method = null;
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                url = request.getRequestURL().toString();
                method = request.getMethod();
                logMsg.append("\r\n---------- ---------- ---------- ---------- ---------- ---------- ---------- ---------- \r\n")
                        .append("开始执行: " + pjp.getSignature())
                        .append("\r\n请求URL: ").append(url)
                        .append("\r\n请求方式: ").append(method)
                        .append("\r\n请求参数: ").append(JSON.toJSONString(pjp.getArgs()));
            } else {
                logMsg.append("\r\n---------- ---------- ---------- ---------- ---------- ---------- ---------- ---------- \r\n")
                        .append("开始执行: " + pjp.getSignature())
                        .append("\r\n请求参数: ").append(Arrays.asList(pjp.getArgs().toString()));
            }
            // 输出日志
            log.info(logMsg.toString());

            // 执行时间监视器
            Stopwatch stopwatch = Stopwatch.createStarted();
            // 用原有的参数调用目标执行方法，获取执行结果
            methodResultObj = pjp.proceed(pjp.getArgs());
            // 如果执行方法的返回值类型为void或String，则认为执行成功，直接构造成功resultDTO
            // 否则，再进行类型强转操作（避免转换异常）
            if (methodResultObj == null || methodResultObj instanceof String) {
                resultDTO = ResultDTO.success();
            } else {
                resultDTO = (ResultDTO) methodResultObj;
            }
            // 将结果转换为JSON字符串
            String methodResultJson = JSON.toJSONString(methodResultObj);
            // 结果执行，记录时间戳
            long consumeTime = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);

            // 将之前StringBuffer清空，重新记录，执行时长，返回值等信息
            logMsg.delete(0, logMsg.length())
                    .append("\r\n结束执行: ").append(pjp.getSignature())
                    .append("\r\n返回值: ").append(methodResultJson)
                    .append("\r\n耗时: ").append(consumeTime).append("(毫秒)")
                    .append("\r\n---------- ---------- ---------- ---------- ---------- ---------- ---------- ---------- \r\n");
            // 输出日志
            log.info(logMsg.toString());

        } catch (Throwable throwable) {
            log.error("服务器内部执行发生异常！", throwable);
            isMethodExeSuccess = false;

            // 获取类的字符串
            String returnClassString = pjp.getSignature().toLongString().split(" ")[1];
            log.info("returnClassString: " + returnClassString);
            // 通过类名创建实例
            methodResultObj = Class.forName(returnClassString).newInstance();
            if (methodResultObj instanceof String) {
                methodResultObj = "服务器内部执行发生异常，请联系管理员！";
            } else {
                resultDTO = (ResultDTO) methodResultObj;
                resultDTO.setCode(ResultCodeEnum.FAILED.getCode());
                resultDTO.setMessage("服务器内部执行发生异常，请联系管理员！");
            }
        }

        // 方法执行成功，或者返回String，则直接返回methodResultObj；否则返回resultDTO
        if (isMethodExeSuccess || methodResultObj instanceof String) {
            return methodResultObj;
        } else {
            log.info("返回returnDTO: " + resultDTO);
            return resultDTO;
        }
    }
}
