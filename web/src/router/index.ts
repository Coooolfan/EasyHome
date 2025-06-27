import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import SearchView from '../views/SearchView.vue'
import ViewView from '../views/ViewView.vue'
import SellView from '../views/SellView.vue'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'home',
            component: HomeView,
        },
        {
            path: '/login',
            name: 'login',
            component: LoginView,
            meta: {
                hideNavbar: true,
            },
        },
        {
            path: '/search',
            name: 'search',
            component: SearchView,
        },
        {
            path: '/view',
            name: 'view',
            component: ViewView,
        },
        {
            path: '/sell',
            name: 'sell',
            component: SellView,
        },
        {
            path: '/profile',
            name: 'profile',
            component: () => import('../views/ProfileView.vue'),
        },
        {
            path: '/my-listings',
            name: 'my-listings',
            component: () => import('../views/MyListingsView.vue'),
        },
        {
            path: '/settings',
            name: 'settings',
            component: () => import('../views/SettingsView.vue'),
        }, 
        {
            path: '/favorites',
            name: 'favorites',
            component: () => import('../views/FavoritesView.vue'),
            meta: {
                requiresAuth: true,
                title: '我的收藏 - EasyHome'
            }
        },
        {
            path: '/notifications',
            name: 'notifications',
            component: () => import('../views/NotificationView.vue'),
        }
    ],
})

export default router
