import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import ChatView from '@/views/ChatView.vue'
import ImportView from '@/views/ImportView.vue'
import GerenciarRegrasView from '@/views/GerenciarRegrasView.vue'
import ProfileView from '@/views/ProfileView.vue'
import HistoricoView from '@/views/HistoricoView.vue'

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
      meta: { requiresAuth: true, requiredRole: 'ADMINISTRADOR' },
    },
    {
      path: '/chat',
      name: 'chat',
      component: ChatView,
      meta: { requiresAuth: true },
    },
    {
      path: '/importar',
      name: 'importar',
      component: ImportView,
      meta: { requiresAuth: true },
    },
    {
      path: '/regras',
      name: 'regras',
      component: GerenciarRegrasView,
      meta: { requiresAuth: true },
    },
    {
      path: '/perfil',
      name: 'perfil',
      component: ProfileView,
      meta: { requiresAuth: true },
    },
    {
      path: '/historico',
      name: 'historico',
      component: HistoricoView,
      meta: { requiresAuth: true },
    },
  ],
})

// Navigation Guard - Protege rotas que exigem autenticação
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.meta.requiresAuth && !token) {
    next({ name: 'login' })
    return
  }

  if (to.meta.requiredRole && role !== to.meta.requiredRole) {
    next({ name: 'chat' })
    return
  }

  // Se já está logado e tenta acessar o login, redireciona
  if (to.name === 'login' && token) {
    next({ name: 'chat' })
    return
  }

  next()
})

export default router
