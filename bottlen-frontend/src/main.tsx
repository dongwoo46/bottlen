import React from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider, Router } from '@tanstack/react-router';
import { routeTree } from './routeTree.gen';
import { TanStackRouterDevtools } from '@tanstack/router-devtools';
import './index.css';

const router = new Router({ routeTree });

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <RouterProvider router={router} />
    {import.meta.env.DEV && <TanStackRouterDevtools router={router} />}
  </React.StrictMode>
);
