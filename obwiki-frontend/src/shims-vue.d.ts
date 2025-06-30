/* eslint-disable */
declare module '*.vue' {
  import type { DefineComponent } from 'vue';
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module '@/utils/session-storage' {
  const SessionStorage: {
    get: (key: string) => any;
    set: (key: string, data: any) => void;
    remove: (key: string) => void;
    clearAll: () => void;
  };
  export default SessionStorage;
}
