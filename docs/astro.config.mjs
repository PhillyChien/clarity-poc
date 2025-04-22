// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

// https://astro.build/config
export default defineConfig({
	integrations: [
		starlight({
			title: 'Documentation',
			social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/withastro/starlight' }],
			sidebar: [
				{
					label: 'Start Here',
					items: [
						{
							label: 'Goals',
							link: '/start-here/goals',
						},
						{
							label: 'Core Features',
							link: '/start-here/core-features',
						},
						{
							label: 'Tech Stack',
							link: '/start-here/tech-stack',
						},
					],
				},
				{
					label: 'Business',
					items: [
						{
							label: 'Roles & Permissions',
							link: '/business/roles-permissions',
						},
						{
							label: 'Todo Management',
							link: '/business/todo-management',
						},
					],
				},
				{
					label: 'Frontend',
					items: [
						{
							label: 'Introduction',
							link: '/frontend/intro',
						},
						{
							label: 'Getting Started',
							link: '/frontend/getting-started',
						},
					],
				},
				{
					label: 'Backend',
					items: [
						{
							label: 'Introduction',
							link: '/backend/intro',
						},
						{	
							label: 'Getting Started',
							link: '/backend/getting-started',
						},
						{
							label: 'Setup RSA Key',
							link: '/backend/setup-rsa-key',
						},
					],
				},
				{
					label: 'Infrastructure',
					items: [
						{
							label: 'Introduction',
							link: '/infrastructure/intro',
						},
						{
							label: 'Getting Started',
							link: '/infrastructure/getting-started',
						},
						{
							label: 'PostgreSQL Managed Identity',
							link: '/infrastructure/pg-managed-identity',
						},
						{
							label: 'Script Reference',
							collapsed: true,
							items: [
								{
									label: 'deploy.sh',
									link: '/infrastructure/script-reference/deploy',
								},
								{
									label: 'store_jwt_secret.sh',
									link: '/infrastructure/script-reference/store-jwt-key',
								},
							],
						},
					],
				},
				{
					label: 'Database',
					items: [
						{
							label: 'Introduction',
							link: '/database/intro',
						},
						{
							label: "Migration Reference",
							collapsed: true,
							items: [
								{
									label: "v1__init_user_table.sql",
									link: "/database/migration-reference/v1",
									attrs: { style: 'font-style: italic; font-size: 12px;' },
								},
								{
									label: "v2__create_todo_folder_tables.sql",
									link: "/database/migration-reference/v2",
									attrs: { style: 'font-style: italic; font-size: 12px;' },
								},
							],
						},
					],
				},
			],
		}),
	],
});
