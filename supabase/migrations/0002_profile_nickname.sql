-- =====================================================================
-- Capricho · Migración 0002: apodo en el perfil + límites de longitud
-- Pegar en Supabase → SQL Editor → Run
-- Guardar en: supabase/migrations/0002_profile_nickname.sql
-- =====================================================================

alter table public.profiles add column if not exists nickname text;

alter table public.profiles drop constraint if exists profiles_display_name_len;
alter table public.profiles add constraint profiles_display_name_len
  check (char_length(display_name) between 1 and 40);

alter table public.profiles drop constraint if exists profiles_nickname_len;
alter table public.profiles add constraint profiles_nickname_len
  check (nickname is null or char_length(nickname) between 1 and 24);

alter table public.profiles drop constraint if exists profiles_salary_max;
alter table public.profiles add constraint profiles_salary_max
  check (monthly_salary <= 1000000000);
