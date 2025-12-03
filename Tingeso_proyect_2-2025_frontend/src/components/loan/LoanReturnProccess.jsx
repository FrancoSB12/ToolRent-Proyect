import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom'; // Para capturar el ID de la URL
import { toast } from 'react-toastify';
import loanService from '../../services/loanService';
import '../../styles/Register.css'; // Reutilizamos estilos de formularios para coherencia
import '../../styles/LoanReturn.css';   // Estilos específicos de tarjetas/lista

// Opciones de daño (Deben coincidir con tu Enum en Java)
const DAMAGE_OPTIONS = [
    { value: 'NO_DANADA', label: 'En Buen Estado' },
    { value: 'EN_EVALUACION', label: 'Dañada (La herramienta irá a evaluación de daños)' }
];

const LoanReturnProcess = () => {
    const { id } = useParams(); // Obtenemos el ID del préstamo desde la ruta /loans/return/:id
    const navigate = useNavigate();

    const [loan, setLoan] = useState(null);
    const [toolConditions, setToolConditions] = useState({}); // Mapa: ID Herramienta -> Nivel de Daño
    const [loading, setLoading] = useState(true);

    // 1. Cargar el Préstamo al iniciar
    useEffect(() => {
        // Asegúrate de tener este método getById en tu loanService
        loanService.getById(id)
            .then(response => {
                const data = response.data;
                setLoan(data);
                
                // Pre-llenamos el estado de las herramientas como "NO_DANADA" por defecto
                const initialConditions = {};
                if (data.loanTools) {
                    data.loanTools.forEach(lt => {
                        // Usamos el ID del ToolItem como clave
                        initialConditions[lt.toolItem.id] = 'NO_DANADA';
                    });
                }
                setToolConditions(initialConditions);
                setLoading(false);
            })
            .catch(err => {
                console.error("Error cargando préstamo:", err);
                toast.error("No se pudo cargar la información del préstamo.");
                navigate('/loan/return'); // Si falla, volvemos al buscador
            });
    }, [id, navigate]);

    // Manejar el cambio de estado en los selectores
    const handleConditionChange = (toolItemId, newCondition) => {
        setToolConditions(prev => ({
            ...prev,
            [toolItemId]: newCondition
        }));
    };

    // 2. Enviar la Devolución
    const handleSubmit = (e) => {
        e.preventDefault();
        if (!loan) return;

        // Construimos el objeto LoanEntity tal como lo espera el Backend
        // (El backend iterará sobre loanTools y actualizará el daño)
        const loanToReturn = {
            id: loan.id,
            loanDate: loan.loanDate,
            returnDate: loan.returnDate,
            lateReturnFee: loan.lateReturnFee,
            status: loan.status,
            validity: loan.validity,
            client: loan.client,
            
            // RECONSTRUIMOS LA LISTA 'loanTools' CON LOS NUEVOS ESTADOS
            loanTools: loan.loanTools.map(lt => ({
                id: lt.id, // ID de la relación intermedia (LoanXToolItem)
                toolItem: {
                    id: lt.toolItem.id,
                    // Inyectamos el nivel de daño seleccionado
                    damageLevel: toolConditions[lt.toolItem.id] 
                }
            }))
        };

        loanService.returnLoan(loan.id, loanToReturn)
            .then(() => {
                toast.success(`Devolución del préstamo #${loan.id} registrada con éxito.`);
                navigate('/loan/return'); // Volver al buscador
            })
            .catch(err => {
                console.error(err);
                const msg = err.response?.data || "Error al procesar la devolución.";
                toast.error(typeof msg === 'string' ? msg : "Error interno del servidor.");
            });
    };

    if (loading) return <div className="full-page-content"><p>Cargando detalles del préstamo...</p></div>;

    return (
        <main className="full-page-content">
            <h2 className="form-title">Procesar Devolución #{loan.id}</h2>

            {/* Información del Préstamo */}
            <div style={{ textAlign: 'center', marginBottom: '2rem', padding: '1rem', background: '#fff', borderRadius: '8px', border: '1px solid #eee' }}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem' }}>
                    <div>
                        <strong>Cliente:</strong><br/>
                        {loan.client?.name} {loan.client?.surname}
                    </div>
                    <div>
                        <strong>RUT:</strong><br/>
                        {loan.client?.run}
                    </div>
                    <div>
                        <strong>Fecha Préstamo:</strong><br/>
                        {loan.loanDate}
                    </div>
                </div>
            </div>

            <form className="register-form" onSubmit={handleSubmit} style={{ maxWidth: '800px' }}>
                <h3 style={{ fontSize: '1.1rem', color: '#333', marginBottom: '1rem', borderBottom: '2px solid #eee', paddingBottom: '0.5rem' }}>
                    Evaluar Estado de las Herramientas
                </h3>

                {/* LISTA DE HERRAMIENTAS A EVALUAR */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    {loan.loanTools?.map(lt => (
                        <div key={lt.toolItem.id} style={{ 
                            padding: '1rem', 
                            border: '1px solid #ccc', 
                            borderRadius: '8px', 
                            background: '#fff', 
                            display: 'flex', 
                            alignItems: 'center', 
                            justifyContent: 'space-between', 
                            flexWrap: 'wrap', 
                            gap: '1rem'
                        }}>
                            
                            {/* Información de la Herramienta */}
                            <div style={{ flex: 1, minWidth: '200px' }}>
                                <strong style={{ fontSize: '1.1rem', display: 'block' }}>
                                    {lt.toolItem.toolType?.name || "Herramienta"}
                                </strong>
                                <span style={{ color: '#666', fontSize: '0.9rem' }}>
                                    Serie: {lt.toolItem.serialNumber}
                                </span>
                                <br/>
                                <span style={{ fontSize: '0.8rem', color: '#888' }}>
                                    Modelo: {lt.toolItem.toolType?.model}
                                </span>
                            </div>

                            {/* Selector de Estado */}
                            <div style={{ flex: 1, minWidth: '250px' }}>
                                <label style={{ fontSize: '0.85rem', fontWeight: 'bold', marginBottom: '5px', display: 'block' }}>
                                    Condición de recepción:
                                </label>
                                <select 
                                    value={toolConditions[lt.toolItem.id]} 
                                    onChange={(e) => handleConditionChange(lt.toolItem.id, e.target.value)}
                                    style={{ 
                                        width: '100%', 
                                        padding: '0.8rem', 
                                        borderRadius: '4px', 
                                        border: '1px solid #999',
                                        backgroundColor: '#f9f9f9',
                                        // Resaltar en rojo si se selecciona un daño
                                        color: toolConditions[lt.toolItem.id] !== 'NO_DANADA' ? '#d9534f' : '#333',
                                        fontWeight: toolConditions[lt.toolItem.id] !== 'NO_DANADA' ? 'bold' : 'normal',
                                        borderColor: toolConditions[lt.toolItem.id] !== 'NO_DANADA' ? '#d9534f' : '#ccc'
                                    }}
                                >
                                    {DAMAGE_OPTIONS.map(opt => (
                                        <option key={opt.value} value={opt.value}>{opt.label}</option>
                                    ))}
                                </select>
                            </div>
                        </div>
                    ))}
                </div>

                {/* Botones de Acción */}
                <div className="form-actions" style={{ marginTop: '2rem', flexDirection: 'row', gap: '1rem', justifyContent: 'center' }}>
                    
                    <button 
                        type="button" 
                        onClick={() => navigate('/loan/return')}
                        style={{ 
                            background: 'none', 
                            border: '1px solid #ccc', 
                            padding: '0.8rem 2rem', 
                            borderRadius: '5px', 
                            cursor: 'pointer', 
                            fontSize: '1rem' 
                        }}
                    >
                        Cancelar
                    </button>

                    <button 
                        type="submit" 
                        className="register-submit-btn" 
                        style={{ backgroundColor: '#28a745', width: 'auto', minWidth: '200px' }}
                    >
                        Confirmar Recepción
                    </button>
                    
                </div>
            </form>
        </main>
    );
};

export default LoanReturnProcess;