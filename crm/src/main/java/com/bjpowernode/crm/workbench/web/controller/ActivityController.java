package com.bjpowernode.crm.workbench.web.controller;

import com.bjpowernode.crm.commons.contants.Contants;
import com.bjpowernode.crm.commons.domain.ReturnObject;
import com.bjpowernode.crm.commons.utils.DateUtils;
import com.bjpowernode.crm.commons.utils.HSSFUtils;
import com.bjpowernode.crm.commons.utils.UUIDUtils;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.settings.service.UserService;
import com.bjpowernode.crm.workbench.domain.Activity;
import com.bjpowernode.crm.workbench.service.ActivityService;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

/**
 * @author 冠军
 * @version 1.0
 */
@Controller
public class ActivityController {
    @Autowired
    private UserService userService;

    @Autowired
    private ActivityService activityService;

    @RequestMapping("/workbench/activity/index.do")
    public String index(HttpServletRequest request){
        //调用service层方法，查询所有的用户
        List<User> userList = userService.queryAllUsers();
        //把数据放到request里面
        request.setAttribute("userList",userList);
        //请求转发到市场活动的主页面
        return "workbench/activity/index";
    }

    @RequestMapping("/workbench/activity/saveCreateActivity.do")
    @ResponseBody
    public Object saveCreateActivity(Activity activity, HttpSession session){
        User user = (User)session.getAttribute(Contants.SESSION_USER);
        //封装参数
        activity.setId(UUIDUtils.getUUID());
        activity.setCreateTime(DateUtils.formatDateTime(new Date()));
        activity.setCreateBy(user.getId());

        ReturnObject returnObject = new ReturnObject();

        //调用service层方法，保存创建的市场活动
        try {
            int ret = activityService.saveCreateActivity(activity);
            if (ret>0){
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_SUCCESS );
            }else {
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage("系统忙，请稍后重试...");
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("系统忙，请稍后重试...");

        }

        return returnObject;
    }

    @RequestMapping("/workbench/activity/queryActivityByConditionForPage.do")
    @ResponseBody
    public Object queryActivityByConditionForPage(String name,String owner,String startDate,String endDate,
                                                  int pageNo,int pageSize){
        //封装参数
        Map<String,Object> map = new HashMap<>();
        map.put("name",name);
        map.put("owner",owner);
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        map.put("beginNo",(pageNo-1)*pageSize);
        map.put("pageSize",pageSize);
        //调用service层方法，查询数据
        List<Activity> activityList = activityService.queryActivityByConditionForPage(map);
        int totalRows = activityService.queryCountOfActivityByCondition(map);
        //调用查询结果，生成响应信息
        Map<String,Object> retMap = new HashMap<>();
        retMap.put("activityList",activityList);
        retMap.put("totalRows",totalRows);
        return retMap;
    }


    public Object deleteActivityByIds(String[] id){
        ReturnObject returnObject = new ReturnObject();
        try {
            //调用service层方法，删除市场活动
            int ret = activityService.deleteActivityByIds(id);
            if (ret>0){
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_SUCCESS);
            }else {
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage("系统忙，请稍后重试...");
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("系统忙，请稍后重试...");
        }
        return returnObject;

    }

    @RequestMapping("/workbench/activity/queryActivityById.do")
    @ResponseBody
    public Object queryActivityById(String id){
        //调用service层方法，查询市场活动
        Activity activity = activityService.queryActivityById(id);
        //根据查询结果，返回响应信息
        return  activity;
    }

    @RequestMapping("/workbench/activity/saveEditActivity.do")
    @ResponseBody
    public Object saveEditActivity(Activity activity,HttpSession session){
        User user = (User)session.getAttribute(Contants.SESSION_USER);
        //封装参数
        activity.setEditTime(DateUtils.formatDateTime(new Date()));
        activity.setEditBy(user.getId());

        ReturnObject returnObject = new ReturnObject();

        try {
            //调用service层方法，保存修改的市场的活动
            int ret = activityService.saveEditActivity(activity);
            if (ret>0){
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_SUCCESS);
            }else {
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage("系统忙，请稍后重试...");
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("系统忙，请稍后重试...");
        }
        return returnObject;
    }

    @RequestMapping("/workbench/activity/fileDownload.do")
    public void fileDownload(HttpServletResponse response) throws Exception{
        //1.设置响应类型application表示应用，octet-stream表示二进制
        response.setContentType("application/octet-stream;charset=UTF-8");
        //2.获取输出流
        ServletOutputStream out = response.getOutputStream();

        //浏览器接收到响应信息之后，默认情况下，直接在显示窗口中打开响应信息；
        // 即使打不开，也会调用应用程序打开；只有实在打不开，才会激活文件下载窗口
        //可以设置响应头信息，是浏览器接收到响应信息之后，直接激活文件下载窗口；即使能打开也不打开
        //Content-Disposition浏览器接收到响应之后打开的方式；attachment以附件的形势（就是下载）打开。
        //mystudentList.xls默认以这个名字下载文件。
        response.addHeader("Content-Disposition","attachment;filename=mystudentList.xls");

        //读取excel文件（InputStream),把输出到浏览器（OutputSteam）
        InputStream is = new FileInputStream("D:\\CrmProject\\项目资料\\serverDir\\studentList2.xls");
        byte[] buff = new byte[256];
        int len=0;
        while ((len= is.read(buff))!=-1){
            out.write(buff,0,len);
        }

        //关闭资源
        is.close();
        out.flush();
    }

    @RequestMapping("/workbench/activity/exportAllActivitys.do")
    public void exportAllActivitys(HttpServletResponse response) throws  Exception{
        //调用service层方法，查询所有的市场活动
        List<Activity> activityList = activityService.queryAllActivitys();
        //创建excel文件，并且把activityList写入到excel文件中
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("市场活动列表");
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        cell.setCellValue("ID");
        cell= row.createCell(1);
        cell.setCellValue("所有者");
        cell= row.createCell(2);
        cell.setCellValue("名称");
        cell= row.createCell(3);
        cell.setCellValue("开始日期");
        cell= row.createCell(4);
        cell.setCellValue("结束日期");
        cell= row.createCell(5);
        cell.setCellValue("成本");
        cell= row.createCell(6);
        cell.setCellValue("描述");
        cell= row.createCell(7);
        cell.setCellValue("创建时间");
        cell= row.createCell(8);
        cell.setCellValue("创建者");
        cell= row.createCell(9);
        cell.setCellValue("修改时间");
        cell= row.createCell(10);
        cell.setCellValue("修改者");

        //遍历activityList,创建HSSFRow对象，生成所有的数据行
        if (activityList!= null && activityList.size()>0){
            Activity activity = null;
            for (int i = 0; i < activityList.size(); i++) {
                activity= activityList.get(i);

                //每遍历出一个activity,生成一行
                row = sheet.createRow(i+1);
                //每一行创建11列，每一列的数据从activity中获取
                cell = row.createCell(0);
                cell.setCellValue(activity.getId());
                cell= row.createCell(1);
                cell.setCellValue(activity.getOwner());
                cell= row.createCell(2);
                cell.setCellValue(activity.getName());
                cell= row.createCell(3);
                cell.setCellValue(activity.getStartDate());
                cell= row.createCell(4);
                cell.setCellValue(activity.getEndDate());
                cell= row.createCell(5);
                cell.setCellValue(activity.getCost());
                cell= row.createCell(6);
                cell.setCellValue(activity.getDescription());
                cell= row.createCell(7);
                cell.setCellValue(activity.getCreateTime());
                cell= row.createCell(8);
                cell.setCellValue(activity.getCreateBy());
                cell= row.createCell(9);
                cell.setCellValue(activity.getEditTime());
                cell= row.createCell(10);
                cell.setCellValue(activity.getEditBy());
            }
        }
        //根据wb对象生成excel文件
//        OutputStream os = new FileOutputStream("D:\\CrmProject\\项目资料\\serverDir\\activityList1.xls");
//        wb.write(os);

        //关闭资源
//        os.close();
//        wb.close();

        //把生成的excel文件下载到客户端
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.addHeader("Content-Disposition","attachment;filename=activityList1.xls");
        OutputStream out = response.getOutputStream();
//        InputStream is = new FileInputStream("D:\\CrmProject\\项目资料\\serverDir\\activityList1.xls");
//        byte[] buff = new byte[256];
//        int len=0;
//        while ((len=is.read(buff))!=0){
//            out.write(buff,0,len);
//        }
        wb.write(out);
        wb.close();
        out.flush();
    }

    @RequestMapping("/workbench/activity/importActivity.do")
    @ResponseBody
    public Object importActivity(MultipartFile activityFile,String userName,HttpSession session){
        System.out.println("username="+userName);
        User user = (User)session.getAttribute(Contants.SESSION_USER);
        ReturnObject returnObject = new ReturnObject();

        try {
            //把excel文件写到磁盘目录中
            String originalFilename = activityFile.getOriginalFilename();
            File file = new File("D:\\CrmProject\\项目资料\\serverDir",originalFilename);
            activityFile.transferTo(file);

            //解析excel文件，获取文件中的数据，并封装成activityList
            //根据excel文件生成HSSFWorkbook对象，封装了excel文件的所有信息
            InputStream is = new FileInputStream("D:\\CrmProject\\项目资料\\serverDir\\" + originalFilename);
            HSSFWorkbook wb = new HSSFWorkbook(is);
            //根据wb获取HSSFSheet对象，封装了一页的所有信息
            HSSFSheet sheet = wb.getSheetAt(0);//页的下标，下标从0开始，依次增加
            //根据sheet获取HSSFWork对象，封装了一行所有信息
            HSSFRow row = null;
            HSSFCell cell = null;
            Activity activity = null;
            List<Activity> activityList = new ArrayList<>();
            for (int i = 1;i <= sheet.getLastRowNum();i++){
                row=sheet.getRow(i);//行的下标，下标从0开始，依次增加
                activity = new Activity();
                activity.setId(UUIDUtils.getUUID());
                activity.setOwner(user.getId());
                activity.setCreateTime(DateUtils.formatDateTime(new Date()));
                activity.setCreateBy(user.getId());

                for (int j = 0;j<row.getLastCellNum();j++){
                    //根据row获取HSSFCell对象，封装了一列的所有信息
                    cell = row.getCell(j);//列的下标，下标从0开始，依次增加

                    //获取列中的数据
                    String cellValue = HSSFUtils.getCellValueForStr(cell);
                    if(j==0){
                        activity.setName(cellValue);
                    } else if (j == 1) {
                        activity.setStartDate(cellValue);
                    } else if (j == 2) {
                        activity.setEndDate(cellValue);
                    } else if (j == 3) {
                        activity.setCost(cellValue);
                    } else if (j == 4) {
                        activity.setDescription(cellValue);
                    }
                }

                //每一行中所有列都封装完成之后，把activity保存到list中
                activityList.add(activity);
            }
            //调用service层方法，保存市场活动
            int ret = activityService.saveCreateActivityByList(activityList);

            returnObject.setCode(Contants.RETURN_OBJECT_CODE_SUCCESS);
            returnObject.setRetData(ret);
        } catch (Exception e) {
            e.printStackTrace();
            returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("系统忙，请稍后重试...");

        }
        return returnObject;
    }

}
