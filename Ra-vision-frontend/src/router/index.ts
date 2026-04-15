import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import ChatView from '@/views/ChatView.vue'
import ImportView from '@/views/ImportView.vue'
import BusinessRulesView from '@/views/BusinessRulesView.vue'
import ProfileView from '@/views/ProfileView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/cadastro',
      name: 'register',
      component: RegisterView,
    },
    {
      path: '/chat',
      name: 'chat',
      component: ChatView,
    },
    {
      path: '/importar',
      name: 'importar',
      component: ImportView,
    },
    {
      path: '/regras',
      name: 'regras',
      component: BusinessRulesView,
    },
    {
      path: '/perfil',
      name: 'perfil',
      component: ProfileView,
    },
  ],
})

export default router
