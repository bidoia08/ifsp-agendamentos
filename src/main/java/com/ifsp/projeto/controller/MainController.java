package com.ifsp.projeto.controller;

import com.ifsp.projeto.model.Agendamento;
import com.ifsp.projeto.model.Usuario;
import com.ifsp.projeto.repository.AgendamentoRepository;
import com.ifsp.projeto.repository.UsuarioRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private AgendamentoRepository agendamentoRepo;

    // === LOGIN ===
    @GetMapping("/")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String nome,
                    @RequestParam String senha,
                    Model model,
                    HttpSession session) {

        var user = usuarioRepo.findByNomeUsuarioAndSenha(nome, senha);

        if (user.isPresent()) {
            // ✅ Guarda o usuário logado na sessão
            session.setAttribute("usuarioLogado", user.get());
            return "redirect:/principal";
        }

        model.addAttribute("erro", "Usuário ou senha inválidos!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }



    // === CADASTRO ===
    @GetMapping("/cadastro")
    public String cadastroForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        // opcional: checar se nomeUsuario já existe
        if (usuarioRepo.findByNomeUsuario(usuario.getNomeUsuario()).isPresent()) {
            redirectAttributes.addFlashAttribute("erro", "Nome de usuário já existe.");
            return "redirect:/cadastro";
        }
        usuarioRepo.save(usuario);
        redirectAttributes.addFlashAttribute("mensagem", "Cadastro realizado com sucesso! Faça login.");
        return "redirect:/";
    }


    // === PRINCIPAL ===
    @GetMapping("/principal")
    public String principal() {
        return "principal";
    }

    // === AGENDAR ===
    @GetMapping("/agendar")
    public String agendarForm(Model model) {
        model.addAttribute("agendamento", new Agendamento());
        return "agendar";
    }

    @PostMapping("/agendar")
    public String salvarAgendamento(@ModelAttribute Agendamento agendamento,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        LocalDate hoje = LocalDate.now();

        // Validação: data passada
        if (agendamento.getData().isBefore(hoje)) {
            model.addAttribute("agendamento", agendamento);
            model.addAttribute("erro", "⚠ Não é possível agendar uma data passada!");
            return "agendar";
        }

        // Validação: horário início < término
        if (agendamento.getHoraInicio().isAfter(agendamento.getHoraFim()) ||
            agendamento.getHoraInicio().equals(agendamento.getHoraFim())) {
            model.addAttribute("agendamento", agendamento);
            model.addAttribute("erro", "⚠ O horário de início deve ser anterior ao horário de término!");
            return "agendar";
        }

        // Verifica conflitos — passando o id para ignorar o próprio agendamento se for edição
        boolean existeConflito = agendamentoRepo.verificarConflitos(
                agendamento.getSala(),
                agendamento.getData(),
                agendamento.getHoraInicio(),
                agendamento.getHoraFim(),
                agendamento.getId()
        );

        if (existeConflito) {
            model.addAttribute("agendamento", agendamento);
            model.addAttribute("erro", "⚠ Já existe um agendamento para esta sala e horário!");
            return "agendar";
        }

        // Salva (cria ou atualiza conforme presença do id)
        agendamentoRepo.save(agendamento);

        redirectAttributes.addFlashAttribute("mensagem", "✅ Agendamento realizado com sucesso!");
        return "redirect:/ver-agendamentos";
    }

    // === VER AGENDAMENTOS ===
    @GetMapping("/ver-agendamentos")
    public String verAgendamentos(HttpSession session, Model model) {
        //dando bo, rever no gpt terça
        List<Agendamento> agendamentos = agendamentoRepo.findAll();
        model.addAttribute("agendamentos", agendamentos);

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        model.addAttribute("usuarioLogado", usuarioLogado);

        return "ver-agendamentos";
    }

    // === EDITAR ===
    @GetMapping("/editar/{id}")
    public String editarAgendamento(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var ag = agendamentoRepo.findById(id).orElse(null);
        if (ag == null) {
            redirectAttributes.addFlashAttribute("erro", "Agendamento não encontrado.");
            return "redirect:/ver-agendamentos";
        }
        model.addAttribute("agendamento", ag);
        return "agendar";
    }

    @GetMapping("/excluir/{id}")
    public String excluirAgendamento(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {

        // Verifica se há alguém logado
        Usuario logado = (Usuario) session.getAttribute("usuarioLogado");

        if (logado == null) {
            redirectAttributes.addFlashAttribute("erro", "Você precisa estar logado!");
            return "redirect:/";
        }

        // ✅ Somente admin pode excluir
        if (!"ADMIN".equalsIgnoreCase(logado.getTipo())) {
            redirectAttributes.addFlashAttribute("erro", "Apenas administradores podem excluir agendamentos!");
            return "redirect:/ver-agendamentos";
        }

        agendamentoRepo.deleteById(id);
        redirectAttributes.addFlashAttribute("mensagem", "🗑️ Agendamento excluído com sucesso!");
        return "redirect:/ver-agendamentos";
    }

    @GetMapping("/recuperar-senha")
    public String mostrarRecuperarSenhaForm() {
        return "recuperar-senha";
    }
    
    @PostMapping("/recuperar-senha")
    public String recuperarSenha(@RequestParam String nomeUsuario,
                                 @RequestParam String palavraSeguranca,
                                 Model model) {
        Usuario usuario = usuarioRepo.findByNomeUsuarioAndPalavraSeguranca(nomeUsuario, palavraSeguranca);
    
        if (usuario != null) {
            model.addAttribute("mensagem", "Sua senha é: " + usuario.getSenha());
        } else {
            model.addAttribute("erro", "Usuário ou palavra de segurança incorretos.");
        }
        return "recuperar-senha";
    }    
}