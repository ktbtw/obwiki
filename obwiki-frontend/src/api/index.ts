// 初始化axios
import axios from 'axios';
console.log("1报错否？")
const api = axios.create({
  baseURL: "http://127.0.0.1:8880/obwiki", // 使用环境变量设置基础URL
  timeout: 10000, // 设置请求超时时间为10秒
});
console.log("2报错否？")
// 添加请求拦截器
/*
* 使用axios拦截器打印请求和响应的数据
* */
api.interceptors.request.use(function (config){
  console.log('请求参数：',config);

  return config;
},error => {
  return Promise.reject(error);
});
console.log("3报错否？")
api.interceptors.response.use(function (response){
  console.log('返回结果：',response);
  return response;
},error => {
  console.log('返回错误：',error);
  return Promise.reject(error);
});
console.log("4报错否？")
// 导出API实例
export default api;
