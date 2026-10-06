-- =====================================================================
-- Capricho · Migración inicial
-- Pegar completo en Supabase → SQL Editor → Run
-- Guardar en: supabase/migrations/0001_init.sql
-- =====================================================================

-- ---------- TABLAS ----------

create table if not exists public.profiles (
  id             uuid primary key references auth.users(id) on delete cascade,
  display_name   text not null,
  monthly_salary numeric(14,2) not null check (monthly_salary > 0),
  currency       text not null default 'ARS',
  created_at     timestamptz not null default now()
);

-- user_id NULL = categoría por defecto, visible para todos
create table if not exists public.categories (
  id         uuid primary key default gen_random_uuid(),
  user_id    uuid references auth.users(id) on delete cascade,
  name       text not null,
  icon       text,
  created_at timestamptz not null default now()
);

-- installments = 1 significa pago inmediato
create table if not exists public.expenses (
  id           uuid primary key default gen_random_uuid(),
  user_id      uuid not null default auth.uid() references auth.users(id) on delete cascade,
  category_id  uuid references public.categories(id) on delete set null,
  title        text,
  amount       numeric(14,2) not null check (amount > 0),
  installments int not null default 1 check (installments >= 1),
  durability   text not null check (durability in ('FLEETING', 'MEDIUM', 'HIGH')),
  kind         text not null default 'ONE_OFF' check (kind in ('RECURRING', 'ANT', 'ONE_OFF')),
  spent_at     date not null default current_date,
  created_at   timestamptz not null default now()
);

create table if not exists public.goals (
  id            uuid primary key default gen_random_uuid(),
  user_id       uuid not null default auth.uid() references auth.users(id) on delete cascade,
  title         text not null,
  target_amount numeric(14,2) not null check (target_amount > 0),
  saved_amount  numeric(14,2) not null default 0 check (saved_amount >= 0),
  installments  int not null default 1 check (installments >= 1),
  durability    text not null check (durability in ('FLEETING', 'MEDIUM', 'HIGH')),
  status        text not null default 'ACTIVE' check (status in ('ACTIVE', 'ACHIEVED', 'ARCHIVED')),
  created_at    timestamptz not null default now()
);

-- ---------- ÍNDICES ----------

create index if not exists expenses_user_date_idx on public.expenses (user_id, spent_at desc);
create index if not exists goals_user_idx         on public.goals (user_id);
create index if not exists categories_user_idx    on public.categories (user_id);

-- ---------- ROW LEVEL SECURITY ----------

alter table public.profiles   enable row level security;
alter table public.categories enable row level security;
alter table public.expenses   enable row level security;
alter table public.goals      enable row level security;

-- profiles: cada usuario ve y edita solo su fila
create policy "profiles_select_own" on public.profiles
  for select to authenticated using ((select auth.uid()) = id);
create policy "profiles_insert_own" on public.profiles
  for insert to authenticated with check ((select auth.uid()) = id);
create policy "profiles_update_own" on public.profiles
  for update to authenticated
  using ((select auth.uid()) = id) with check ((select auth.uid()) = id);

-- categories: ve las por defecto (user_id null) y las propias; solo modifica las propias
create policy "categories_select" on public.categories
  for select to authenticated
  using (user_id is null or (select auth.uid()) = user_id);
create policy "categories_insert_own" on public.categories
  for insert to authenticated with check ((select auth.uid()) = user_id);
create policy "categories_update_own" on public.categories
  for update to authenticated
  using ((select auth.uid()) = user_id) with check ((select auth.uid()) = user_id);
create policy "categories_delete_own" on public.categories
  for delete to authenticated using ((select auth.uid()) = user_id);

-- expenses y goals: acceso total solo a las filas propias
create policy "expenses_all_own" on public.expenses
  for all to authenticated
  using ((select auth.uid()) = user_id) with check ((select auth.uid()) = user_id);

create policy "goals_all_own" on public.goals
  for all to authenticated
  using ((select auth.uid()) = user_id) with check ((select auth.uid()) = user_id);

-- ---------- CATEGORÍAS POR DEFECTO ----------

insert into public.categories (user_id, name, icon) values
  (null, 'Vivienda',           'home'),
  (null, 'Comida',             'restaurant'),
  (null, 'Transporte',         'bus'),
  (null, 'Transporte privado', 'car'),
  (null, 'Salud',              'health'),
  (null, 'Ropa',               'shirt'),
  (null, 'Juegos',             'gamepad'),
  (null, 'Otros',              'more');
