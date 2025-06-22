import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import SearchView from '../views/SearchView.vue'
import ViewView from '../views/ViewView.vue'
import SellView from '../views/SellView.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'home',
            component: HomeView,
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
    ],
})

export default router
