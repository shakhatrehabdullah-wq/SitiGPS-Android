package com.siti.gps;

import android.content.Context;
import android.content.pm.PackageManager;
import org.json.JSONObject;

/** Optional public Android Automotive read-only API. No CAN writes or OEM bypasses. */
public final class VehicleReader {
  private final Context context;
  private Object car,manager;
  private Class<?> carClass,ids;
  public VehicleReader(Context context){this.context=context;}
  public boolean supported(){
    try{Class.forName("android.car.Car");Class.forName("android.car.VehiclePropertyIds");return true;}
    catch(Exception|LinkageError e){return false;}
  }
  private Object read(String name,Class<?> type){
    try{
      int id=ids.getField(name).getInt(null);
      Object property=manager.getClass().getMethod("getProperty",Class.class,int.class,int.class).invoke(manager,type,id,0);
      if(property==null || ((Number)property.getClass().getMethod("getStatus").invoke(property)).intValue()!=0)return null;
      return property.getClass().getMethod("getValue").invoke(property);
    }catch(Exception|LinkageError e){return null;}
  }
  private Double number(String name){Object value=read(name,Float.class);if(!(value instanceof Number))return null;double n=((Number)value).doubleValue();return Double.isNaN(n)||Double.isInfinite(n)?null:n;}
  public synchronized String snapshot(){
    JSONObject result=new JSONObject(),data=new JSONObject();
    try{
      result.put("connected",false);result.put("controlsAvailable",false);
      if(!supported()){result.put("message","نظام الشاشة لا يوفّر واجهة بيانات سيارة متوافقة؛ يلزم ربط خاص لـ BYD");return result.toString();}
      if(context.checkSelfPermission("android.car.permission.CAR_ENERGY")!=PackageManager.PERMISSION_GRANTED){result.put("message","إذن قراءة بيانات طاقة السيارة غير متاح");return result.toString();}
      if(car==null){carClass=Class.forName("android.car.Car");ids=Class.forName("android.car.VehiclePropertyIds");car=carClass.getMethod("createCar",Context.class).invoke(null,context);}
      if(car==null){result.put("message","خدمة بيانات السيارة غير متصلة");return result.toString();}
      if(manager==null)manager=carClass.getMethod("getCarManager",String.class).invoke(car,"property");
      if(manager==null){result.put("message","خدمة قراءات السيارة غير متاحة");return result.toString();}
      Double wh=number("EV_BATTERY_LEVEL"),capacity=number("EV_CURRENT_BATTERY_CAPACITY"),range=number("RANGE_REMAINING"),rate=number("EV_BATTERY_INSTANTANEOUS_CHARGE_RATE"),temperature=number("EV_BATTERY_AVERAGE_TEMPERATURE");
      if(wh!=null&&wh>=0)data.put("energyKwh",wh/1000);
      if(wh!=null&&capacity!=null&&capacity>0&&wh>=0&&wh<=capacity)data.put("batteryPercent",wh/capacity*100);
      if(range!=null&&range>=0)data.put("rangeKm",range/1000);
      if(rate!=null)data.put("powerKw",rate/1000000);
      if(temperature!=null)data.put("batteryC",temperature);
      Object plugged=read("EV_CHARGE_PORT_CONNECTED",Boolean.class);if(plugged instanceof Boolean)data.put("plugged",plugged);
      result.put("connected",data.length()>0);result.put("readings",data);result.put("updatedAt",System.currentTimeMillis());
      result.put("message",data.length()>0?"قراءات فعلية من نظام السيارة · القراءة فقط":"لا توجد قراءات متاحة لهذا النظام أو الصلاحيات");
    }catch(Exception|LinkageError e){try{result.put("connected",false);result.put("message","تعذّر الاتصال بخدمة بيانات السيارة");}catch(Exception ignored){}}
    return result.toString();
  }
  public synchronized void close(){try{if(car!=null&&carClass!=null)carClass.getMethod("disconnect").invoke(car);}catch(Exception ignored){}car=null;manager=null;}
}

