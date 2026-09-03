import { useState } from 'react'
import { iniciarSesion } from './services/authService'
import { consultarClientes } from './services/clienteService'


function App() {
  const [nombreUsuario, setNombreUsuario] = useState('')
  const [password, setPassword] = useState('')
  const [mensaje, setMensaje] = useState('')
  const [cargando, setCargando] = useState(false)
  const [token, setToken] = useState('')
  const [clientes, setClientes] = useState([])
  const [cargandoClientes, setCargandoClientes] = useState(false)
  const [textoBusqueda, setTextoBusqueda] = useState('')


  async function manejarConsultaClientes() {
    setCargandoClientes(true)
    setMensaje('')

    try {
      const datos = await consultarClientes(token, textoBusqueda)
      setClientes(datos.contenido)
    } catch (error) {
      setMensaje(error.message)
    } finally {
      setCargandoClientes(false)
    }
  }

  async function manejarLogin(evento) {
    evento.preventDefault()
    setMensaje('')
    setCargando(true)

    try {
      const datos = await iniciarSesion(nombreUsuario, password)
      setToken(datos.token)
      setPassword('')
      setMensaje(`Bienvenido ${datos.nombreUsuario}`)
    } catch (error) {
      setMensaje(error.message)
    } finally {
      setCargando(false)
    }
  }
  function cerrarSesion() {
    setToken('')
    setClientes([])
    setNombreUsuario('')
    setPassword('')
    setMensaje('Sesión cerrada')
  }





  return (
    <main>
      <h1>Gestión del gimnasio</h1>
      {!token && (
        <form onSubmit={manejarLogin}>
          <div>
            <label htmlFor="nombreUsuario">Usuario</label>
            <input
              id="nombreUsuario"
              type="text"
              value={nombreUsuario}
              onChange={(evento) => setNombreUsuario(evento.target.value)}
              autoComplete="username"
              required
            />
          </div>

          <div>
            <label htmlFor="password">Contraseña</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(evento) => setPassword(evento.target.value)}
              autoComplete="current-password"
              required
            />
          </div>

          <button type="submit" disabled={cargando}>
            {cargando ? 'Ingresando...' : 'Ingresar'}</button>

        </form>
      )}
      <p role="status">{mensaje}</p>

      {token && <p>Sesión iniciada. Ya podés consultar clientes.</p>}

      {token && (
        <div>
          <label htmlFor="busqueda">Buscar por nombre o DNI</label>

          <input
            id="busqueda"
            type="search"
            value={textoBusqueda}
            onChange={(evento) => setTextoBusqueda(evento.target.value)}
            placeholder="Ejemplo: Ana o 30111222"
          />
          <section>
            <button
              type="button"
              onClick={cerrarSesion}
              disabled={cargandoClientes}
            >
              Cerrar sesión
            </button>
            <button
              type="button"
              onClick={manejarConsultaClientes}
              disabled={cargandoClientes}
            >
              {cargandoClientes ? 'Consultando...' : 'Consultar clientes'}
            </button>

            <ul>
              {clientes.map((cliente) => (
                <li key={cliente.id}>
                  {cliente.nombre} {cliente.apellido} — DNI: {cliente.dni}
                </li>
              ))}
            </ul>
          </section>
        </div>
      )}
    </main>
  )
}



export default App