// 初始化axios
import store from '@/store';
import { Modal } from 'ant-design-vue';
import axios from 'axios';

declare global {
  interface Window {
    __hasLoginModal?: boolean;
  }
}

const api = axios.create({
  baseURL: "http://127.0.0.1:8880/obwiki", // 使用环境变量设置基础URL
  timeout: 60000, // 设置请求超时时间为60秒
});

// 添加请求拦截器
/*
* 使用axios拦截器打印请求和响应的数据
* */


api.interceptors.request.use(function (config: any) {
  // 判断是否需要登录
  if(config.url.includes('/login')||config.url.includes('/save')||config.url.includes('/ai-chat')){
    return config;
  }
    const token = store.state.user.token;
    if (!token) {
      if (!window.__hasLoginModal) {
        window.__hasLoginModal = true;
        Modal.warning({
          title: '请先登录',
          content: '您需要登录后才能进行此操作',
          onOk: () => {
            window.__hasLoginModal = false;
          }
        });
      }
      // 阻止请求
      return new Promise(() => {});
    }
  if (token) {
    config.headers.token = token;
  }
  // 统一加上utf-8编码
  config.headers['Content-Type'] = 'application/json; charset=utf-8';
  return config;
}, error => {
  return Promise.reject(error);
});

// 导出API实例
export default api;
