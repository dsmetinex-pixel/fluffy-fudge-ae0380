package com.example.data.remote.supabase

object SupabaseConfig {
    const val DEFAULT_PROJECT_ID = "bzzmtjoejpdfiqrzaqlg"
    const val DEFAULT_BASE_URL = "https://bzzmtjoejpdfiqrzaqlg.supabase.co"
    const val DEFAULT_API_KEY = "sb_publishable_MmxQxwce5CKtuysitXHLCw_rrz30CI8"

    const val TABLE_PRODUCTS = "products"
    const val TABLE_STOCK_IN = "stock_in"
    const val TABLE_STOCK_OUT = "stock_out"
    const val TABLE_ACTIVITY_LOGS = "activity_logs"

    val SQL_SETUP_SCRIPT = """
-- METINEX ALUMINIUM - SUPABASE SCHEMA INITIALIZATION
-- Run this script in your Supabase SQL Editor:
-- https://supabase.com/dashboard/project/$DEFAULT_PROJECT_ID/sql

-- 1. Products Master Catalog
CREATE TABLE IF NOT EXISTS public.products (
    product_id TEXT PRIMARY KEY,
    product_name TEXT NOT NULL,
    hsm_code TEXT NOT NULL,
    category TEXT NOT NULL,
    unit TEXT NOT NULL,
    rate_per_unit NUMERIC NOT NULL DEFAULT 0,
    opening_stock NUMERIC NOT NULL DEFAULT 0,
    minimum_stock NUMERIC NOT NULL DEFAULT 0,
    current_stock NUMERIC NOT NULL DEFAULT 0,
    description TEXT DEFAULT '',
    created_at BIGINT DEFAULT (extract(epoch from now()) * 1000)::bigint,
    updated_at BIGINT DEFAULT (extract(epoch from now()) * 1000)::bigint
);

-- 2. Stock In Transactions Table
CREATE TABLE IF NOT EXISTS public.stock_in (
    transaction_id TEXT PRIMARY KEY,
    product_id TEXT NOT NULL REFERENCES public.products(product_id) ON DELETE CASCADE,
    product_name TEXT NOT NULL,
    quantity NUMERIC NOT NULL,
    unit TEXT NOT NULL,
    previous_stock NUMERIC NOT NULL,
    hsm_code TEXT NOT NULL,
    rate NUMERIC NOT NULL,
    total_amount NUMERIC NOT NULL,
    new_stock NUMERIC NOT NULL,
    date TEXT NOT NULL,
    firm_name TEXT NOT NULL,
    created_at BIGINT DEFAULT (extract(epoch from now()) * 1000)::bigint,
    created_by TEXT DEFAULT 'Admin (dsmetinex@gmail.com)'
);

-- 3. Stock Out Transactions Table
CREATE TABLE IF NOT EXISTS public.stock_out (
    transaction_id TEXT PRIMARY KEY,
    product_id TEXT NOT NULL REFERENCES public.products(product_id) ON DELETE CASCADE,
    product_name TEXT NOT NULL,
    party_name TEXT NOT NULL,
    quantity NUMERIC NOT NULL,
    unit TEXT NOT NULL,
    previous_stock NUMERIC NOT NULL,
    hsm_code TEXT NOT NULL,
    rate NUMERIC NOT NULL,
    total_amount NUMERIC NOT NULL,
    remaining_stock NUMERIC NOT NULL,
    date TEXT NOT NULL,
    firm_name TEXT NOT NULL,
    created_at BIGINT DEFAULT (extract(epoch from now()) * 1000)::bigint,
    created_by TEXT DEFAULT 'Admin (dsmetinex@gmail.com)'
);

-- 4. Activity Audit Logs Table
CREATE TABLE IF NOT EXISTS public.activity_logs (
    log_id TEXT PRIMARY KEY,
    user_id TEXT DEFAULT 'usr_admin',
    user_name TEXT DEFAULT 'Admin User',
    action TEXT NOT NULL,
    description TEXT NOT NULL,
    product_name TEXT DEFAULT '',
    transaction_type TEXT DEFAULT '',
    quantity NUMERIC DEFAULT 0,
    previous_stock NUMERIC DEFAULT 0,
    updated_stock NUMERIC DEFAULT 0,
    date TEXT NOT NULL,
    timestamp BIGINT DEFAULT (extract(epoch from now()) * 1000)::bigint
);

-- Enable Row Level Security (RLS)
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.stock_in ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.stock_out ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.activity_logs ENABLE ROW LEVEL SECURITY;

-- Allow Public / Anon access for app synchronization
DROP POLICY IF EXISTS "Allow anon all on products" ON public.products;
CREATE POLICY "Allow anon all on products" ON public.products FOR ALL USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Allow anon all on stock_in" ON public.stock_in;
CREATE POLICY "Allow anon all on stock_in" ON public.stock_in FOR ALL USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Allow anon all on stock_out" ON public.stock_out;
CREATE POLICY "Allow anon all on stock_out" ON public.stock_out FOR ALL USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Allow anon all on activity_logs" ON public.activity_logs;
CREATE POLICY "Allow anon all on activity_logs" ON public.activity_logs FOR ALL USING (true) WITH CHECK (true);
    """.trimIndent()
}
