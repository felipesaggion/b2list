import React, { useEffect, useState } from 'react';
import {
    Box, Paper, Typography, Grid, Divider, Chip,
    Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
    Card, CardContent, Stack, Backdrop, CircularProgress, Button
} from '@mui/material';
import {
    Person, Store, Payments, ShoppingBag, ArrowBack, Warehouse // Importado Warehouse
} from '@mui/icons-material';
import { format, parseISO } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import type { OrderDetails } from '../../models/order-details';
import { useParams, useNavigate } from 'react-router-dom';
import { getOrderDetails } from '../../services/order-service';

const OrderDetailsScreen: React.FC = () => {
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const { externalReference } = useParams<{ externalReference: string }>();
    const navigate = useNavigate();

    const formatCurrency = (val: number) =>
        new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val);

    const formatDate = (isoStr: string) =>
        isoStr ? format(parseISO(isoStr), "dd/MM/yyyy HH:mm", { locale: ptBR }) : "";

    const getStatusColor = (status: string): "success" | "error" | "warning" | "default" => {
        switch (status) {
            case 'COMPLETED': return 'success';
            case 'CANCELLED': return 'error';
            default: return 'warning';
        }
    };

    const [order, setOrder] = useState<OrderDetails | null>(null);

    useEffect(() => {
        if (externalReference) {
            getOrderDetails(externalReference)
                .then(data => {
                    setOrder(data as OrderDetails);
                    setIsLoading(false);
                })
                .catch(error => {
                    setIsLoading(false);
                    console.error("Erro ao buscar pedido:", error);
                });
        }
    }, [externalReference]);

    if (isLoading || !order) {
        return (
            <Backdrop sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }} open={true}>
                <Stack alignItems="center" spacing={2}>
                    <CircularProgress color="inherit" size={60} />
                    <Typography variant="h6">Carregando detalhes do pedido...</Typography>
                </Stack>
            </Backdrop>
        );
    }

    return (
        <Box sx={{ p: 3, bgcolor: 'background.default', minHeight: '100vh', color: 'text.primary' }}>
            <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
                <Button
                    variant="outlined"
                    startIcon={<ArrowBack />}
                    onClick={() => navigate('/orders')}
                    sx={{ borderRadius: 2, borderColor: 'divider' }}
                >
                    Voltar
                </Button>
            </Box>

            <Paper sx={{ p: 3, mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderRadius: 2 }}>
                <Box>
                    <Typography variant="h5" sx={{ fontWeight: 'bold' }}>
                        Pedido: {order.externalReference}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                        ID Sistema: {order.code} | Origem: {order.origin}
                    </Typography>
                </Box>
                <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
                    <Typography variant="body2" sx={{ display: { xs: 'none', sm: 'block' } }}>
                        {formatDate(order.createdAt)}
                    </Typography>
                    <Chip
                        label={order.status}
                        color={getStatusColor(order.status)}
                        sx={{ fontWeight: 'bold' }}
                    />
                </Stack>
            </Paper>

            <Grid container spacing={3}>
                <Grid size={{ xs: 12, md: 8 }}>
                    <Stack spacing={3}>
                        <Grid container spacing={2}>
                            <Grid size={{ xs: 12, sm: 6 }}>
                                <Card variant="outlined" sx={{ height: '100%', bgcolor: 'background.paper' }}>
                                    <CardContent>
                                        <Typography variant="subtitle2" color="primary" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                            <Person fontSize="small" /> Dados do Comprador
                                        </Typography>
                                        <Typography variant="body1" sx={{ fontWeight: 'bold' }}>{order.buyer.name}</Typography>
                                        <Typography variant="body2" color="text.secondary">Ref. Externa: {order.buyer.externalReference}</Typography>
                                        <Divider sx={{ my: 1.5 }} />
                                        <Typography variant="caption" color="text.secondary" display="block">Limite de Crédito</Typography>
                                        <Typography variant="body1" color="success.main" sx={{ fontWeight: 'medium' }}>
                                            {formatCurrency(order.buyer.creditLimit)}
                                        </Typography>
                                    </CardContent>
                                </Card>
                            </Grid>

                            <Grid size={{ xs: 12, sm: 6 }}>
                                <Card variant="outlined" sx={{ height: '100%', bgcolor: 'background.paper' }}>
                                    <CardContent>
                                        <Typography variant="subtitle2" color="secondary" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                            <Store fontSize="small" /> Vendedor e Logística
                                        </Typography>
                                        <Typography variant="body1" sx={{ fontWeight: 'bold' }}>{order.seller.name}</Typography>
                                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>Tenant: {order.tenantCode}</Typography>

                                        <Divider sx={{ my: 1.5 }} />

                                        {/* SEÇÃO DA WAREHOUSE */}
                                        <Typography variant="subtitle2" sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                                            <Warehouse fontSize="inherit" color="action" /> Depósito de Origem
                                        </Typography>
                                        <Typography variant="body2" sx={{ fontWeight: 'medium' }}>
                                            {order.warehouse.name}
                                        </Typography>
                                        <Typography variant="caption" color="text.secondary">
                                            Ref. Externa: {order.warehouse.externalReference}
                                        </Typography>
                                    </CardContent>
                                </Card>
                            </Grid>
                        </Grid>

                        <TableContainer component={Paper} variant="outlined" sx={{ borderRadius: 2 }}>
                            <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
                                <ShoppingBag color="action" />
                                <Typography variant="h6">Itens do Pedido</Typography>
                            </Box>
                            <Table size="small">
                                <TableHead sx={{ bgcolor: 'action.hover' }}>
                                    <TableRow>
                                        <TableCell>Produto</TableCell>
                                        <TableCell align="center">Qtd</TableCell>
                                        <TableCell align="right">Preço Un.</TableCell>
                                        <TableCell align="right">Subtotal</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {order.items.map((item, idx) => (
                                        <TableRow key={idx} hover>
                                            <TableCell>
                                                <Typography variant="body2" sx={{ fontWeight: 'medium' }}>{item.productName}</Typography>
                                                <Typography variant="caption" color="text.secondary">{item.productCode}</Typography>
                                            </TableCell>
                                            <TableCell align="center">{item.quantity}</TableCell>
                                            <TableCell align="right">{formatCurrency(item.unitPrice)}</TableCell>
                                            <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatCurrency(item.subtotal)}</TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    </Stack>
                </Grid>

                <Grid size={{ xs: 12, md: 4 }}>
                    <Stack spacing={3}>
                        <Card variant="outlined" sx={{ bgcolor: 'background.paper' }}>
                            <CardContent>
                                <Typography variant="subtitle2" color="success.main" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                    <Payments /> Condição de Pagamento
                                </Typography>
                                <Typography variant="body1" sx={{ fontWeight: 'bold' }}>{order.paymentCondition.description}</Typography>
                                <Typography variant="caption" color="text.secondary">Cód: {order.paymentCondition.code}</Typography>
                                <Divider sx={{ my: 1.5 }} />
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                                    <Typography variant="body2">Desconto Base:</Typography>
                                    <Typography variant="body2" sx={{ fontWeight: 'bold' }}>{order.paymentCondition.discountPercentage}%</Typography>
                                </Box>
                                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                                    <Typography variant="body2">Extra:</Typography>
                                    <Typography variant="body2" color="success.main">{order.paymentCondition.extraDiscountPercentage + order.paymentCondition.operationalFeePercentage}%</Typography>
                                </Box>
                            </CardContent>
                        </Card>

                        <Card sx={{ bgcolor: 'primary.main', color: 'primary.contrastText', boxShadow: 3 }}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom sx={{ opacity: 0.9 }}>
                                    Resumo Financeiro
                                </Typography>
                                <Stack spacing={1}>
                                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                                        <Typography variant="body2">Subtotal:</Typography>
                                        <Typography variant="body2">{formatCurrency(order.subtotal)}</Typography>
                                    </Box>
                                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                                        <Typography variant="body2">Descontos:</Typography>
                                        <Typography variant="body2" sx={{ opacity: 0.8 }}>
                                            - {formatCurrency(order.discountValue)}
                                        </Typography>
                                    </Box>
                                    <Divider sx={{ my: 1, borderColor: 'rgba(255,255,255,0.2)' }} />
                                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>Total</Typography>
                                        <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                                            {formatCurrency(order.total)}
                                        </Typography>
                                    </Box>
                                </Stack>
                            </CardContent>
                        </Card>
                    </Stack>
                </Grid>
            </Grid>
        </Box>
    );
};

export default OrderDetailsScreen;