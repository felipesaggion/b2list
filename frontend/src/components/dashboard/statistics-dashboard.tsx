import React, { useState, useEffect } from 'react';
import {
    Grid, Card, CardContent, Typography, Box, Table, TableBody,
    TableCell, TableContainer, TableHead, TableRow, Paper,
    Button, Divider, ThemeProvider, createTheme, CssBaseline,
    Backdrop,
    CircularProgress,
    Stack
} from '@mui/material';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { ptBR } from 'date-fns/locale/pt-BR';
import {
    TrendingUp, ShoppingCart, XCircle, CheckCircle,
    User, Package, Search
} from 'lucide-react';
import { getOrderStatistics } from '../../services/order-service';
import type { StatisticsResponse } from '../../models/statistics-response';

const darkTheme = createTheme({
    palette: {
        mode: 'dark',
        background: { default: '#0A1929', paper: '#132F4C' },
        primary: { main: '#3399FF' },
        secondary: { main: '#b39ddb' },
    },
});

const StatCard = ({ title, value, icon, color }: { title: string; value: string | number; icon: React.ReactNode; color: string }) => (
    <Card sx={{ height: '100%', backgroundImage: 'none', border: '1px solid rgba(255,255,255,0.1)' }}>
        <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <Box sx={{ p: 1, borderRadius: 2, display: 'flex', backgroundColor: `${color}25`, color: color }}>
                    {icon}
                </Box>
                <Typography variant="subtitle2" sx={{ ml: 1.5, fontWeight: 'bold', color: 'text.secondary' }}>
                    {title}
                </Typography>
            </Box>
            <Typography variant="h5" sx={{ fontWeight: 'bold', color: '#fff' }}>
                {value}
            </Typography>
        </CardContent>
    </Card>
);

const StatisticsDashboard = () => {
    const [data, setData] = useState<StatisticsResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    const [dateFrom, setDateFrom] = useState<Date | null>(new Date(Date.now() - 7 * 24 * 60 * 60 * 1000));
    const [dateTo, setDateTo] = useState<Date | null>(new Date());

    const loadData = () => {
        if (dateFrom && dateTo) {
            setIsLoading(true);
            getOrderStatistics(dateFrom, dateTo)
                .then(res => {
                    setData(res);
                    setIsLoading(false);
                })
                .catch(error => {
                    setIsLoading(false);
                    console.error("Erro ao buscar estatísticas:", error);
                });
        }
    };

    useEffect(() => { 
        loadData();
     }, []);

    const formatCurrency = (val: number) => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val ?? 0);

    if (isLoading && !data) return (
        <Backdrop sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }} open={true}>
            <CircularProgress color="inherit" size={60} />
            <Typography variant="h6" sx={{ ml: 2 }}>Carregando...</Typography>
        </Backdrop>
    );

    const datePickerSx = {
        backgroundColor: 'background.paper',
        borderRadius: 1,
        '& .MuiInputBase-input': { color: '#fff' },
        '& .MuiInputLabel-root': { color: 'rgba(255, 255, 255, 0.7)', '&.Mui-focused': { color: '#fff' } },
        '& .MuiOutlinedInput-root': {
            '& fieldset': { borderColor: 'rgba(255, 255, 255, 0.3)' },
            '&:hover fieldset': { borderColor: '#fff' },
            '&.Mui-focused fieldset': { borderColor: '#3399FF' },
        },
        '& .MuiIconButton-root': { color: '#fff' },
    };

    return (
        <ThemeProvider theme={darkTheme}>
            <CssBaseline />
            <Box sx={{ width: '100%', p: { xs: 1, sm: 3 }, boxSizing: 'border-box', overflowX: 'hidden' }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: { xs: 'flex-start', md: 'center' }, mb: 4, flexDirection: { xs: 'column', md: 'row' }, gap: 2 }}>
                    <Typography variant="h4" sx={{ fontWeight: 'bold', color: 'primary.main' }}>
                        📊 Dashboard
                    </Typography>

                    <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ width: { xs: '100%', md: 'auto' }, alignItems: 'center' }}>
                        <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={ptBR}>
                            <DatePicker
                                label="De"
                                value={dateFrom}
                                onChange={(newValue) => setDateFrom(newValue)}
                                format="dd/MM/yyyy" 
                                slotProps={{ textField: { size: 'small', sx: datePickerSx } }}
                            />
                            <DatePicker
                                label="Até"
                                value={dateTo}
                                onChange={(newValue) => setDateTo(newValue)}
                                format="dd/MM/yyyy"
                                slotProps={{ textField: { size: 'small', sx: datePickerSx } }}
                            />
                        </LocalizationProvider>
                        <Button variant="contained" startIcon={<Search size={18} />} onClick={loadData} sx={{ color: '#fff', fontWeight: 'bold', height: '40px' }}>
                            Filtrar
                        </Button>
                    </Stack>
                </Box>

                {/* KPIs Grid - USANDO SIZE EM VEZ DE ITEM/XS */}
                <Grid container spacing={3} sx={{ mb: 4, width: '100%' }}>
                    <Grid size={{ xs: 12, sm: 6, md: 2.4 }}>
                        <StatCard title="Total Pedidos" value={data?.totalOrders ?? 0} icon={<ShoppingCart />} color="#66b2ff" />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 6, md: 2.4 }}>
                        <StatCard title="Confirmados" value={data?.confirmedOrders ?? 0} icon={<CheckCircle />} color="#66bb6a" />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 6, md: 2.4 }}>
                        <StatCard title="Cancelados" value={data?.cancelledOrders ?? 0} icon={<XCircle />} color="#f44336" />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 6, md: 2.4 }}>
                        <StatCard title="Receita Total" value={formatCurrency(data?.totalRevenue ?? 0)} icon={<TrendingUp />} color="#4fc3f7" />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 6, md: 2.4 }}>
                        <StatCard title="Ticket Médio" value={formatCurrency(data?.averageOrderValue ?? 0)} icon={<TrendingUp />} color="#ce93d8" />
                    </Grid>
                </Grid>

                {/* Tables Grid - USANDO SIZE EM VEZ DE ITEM/XS */}
                <Grid container spacing={3} sx={{ width: '100%' }}>
                    <Grid size={{ xs: 12, md: 6 }}>
                        <TableContainer component={Paper} sx={{ borderRadius: 2, border: '1px solid rgba(255,255,255,0.1)', backgroundImage: 'none', overflow: 'hidden' }}>
                            <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
                                <User size={20} color="#3399FF" />
                                <Typography variant="h6" sx={{ fontWeight: 'bold' }}>Top 5 Compradores</Typography>
                            </Box>
                            <Divider />
                            <Table size="small" sx={{ tableLayout: 'fixed' }}>
                                <TableHead sx={{ backgroundColor: 'rgba(255,255,255,0.05)' }}>
                                    <TableRow>
                                        <TableCell sx={{ color: 'text.secondary' }}>Cliente</TableCell>
                                        <TableCell align="right" sx={{ color: 'text.secondary', width: '80px' }}>Pedidos</TableCell>
                                        <TableCell align="right" sx={{ color: 'text.secondary', width: '120px' }}>Total</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {(data?.topBuyers ?? []).map((buyer: any, index: number) => (
                                        <TableRow key={index} hover>
                                            <TableCell sx={{ fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                                {buyer.name}
                                            </TableCell>
                                            <TableCell align="right">{buyer.orderCount}</TableCell>
                                            <TableCell align="right" sx={{ color: '#66bb6a', fontWeight: 'bold' }}>
                                                {formatCurrency(buyer.totalSpent)}
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    </Grid>

                    <Grid size={{ xs: 12, md: 6 }}>
                        <TableContainer component={Paper} sx={{ borderRadius: 2, border: '1px solid rgba(255,255,255,0.1)', backgroundImage: 'none', overflow: 'hidden' }}>
                            <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
                                <Package size={20} color="#ce93d8" />
                                <Typography variant="h6" sx={{ fontWeight: 'bold' }}>Top 5 Produtos</Typography>
                            </Box>
                            <Divider />
                            <Table size="small" sx={{ tableLayout: 'fixed' }}>
                                <TableHead sx={{ backgroundColor: 'rgba(255,255,255,0.05)' }}>
                                    <TableRow>
                                        <TableCell sx={{ color: 'text.secondary' }}>Código</TableCell>
                                        <TableCell sx={{ color: 'text.secondary' }}>Produto</TableCell>
                                        <TableCell align="right" sx={{ color: 'text.secondary', width: '70px' }}>Qtd</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {(data?.topProducts ?? []).map((product: any, index: number) => (
                                        <TableRow key={index} hover>
                                            <TableCell sx={{ fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                                {product.productCode}
                                            </TableCell>
                                            <TableCell sx={{ fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                                {product.productName}
                                            </TableCell>
                                            <TableCell align="right">{product.totalQuantity}</TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    </Grid>
                </Grid>
            </Box>
        </ThemeProvider>
    );
};

export default StatisticsDashboard;